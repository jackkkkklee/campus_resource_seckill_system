package ind.lcw.campus_seckill.service.ServiceImpl;

import ind.lcw.campus_seckill.entity.OrderInfo;
import ind.lcw.campus_seckill.entity.SeckillOrder;
import ind.lcw.campus_seckill.entity.SeckillUser;
import ind.lcw.campus_seckill.service.ItemService;
import ind.lcw.campus_seckill.service.SeckillService;
import ind.lcw.campus_seckill.util.MD5Util;
import ind.lcw.campus_seckill.util.UUIDUtil;
import ind.lcw.campus_seckill.vo.ItemVo;
import ind.lcw.campus_seckill.redis.RedisService;
import ind.lcw.campus_seckill.redis.SeckillKey;
import ind.lcw.campus_seckill.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    ItemService itemService;
    @Autowired
    OrderService orderService;
    @Autowired
    RedisService redisService;
    /**
     * 秒杀，原子操作：1.库存减1，2.下订单，3.写入秒杀订单--->是一个事务
     * 返回生成的订单
     * @param user
     * @param itemVo
     * @return
     */
    @Transactional
    @Override
    public OrderInfo miaosha1(SeckillUser user, ItemVo itemVo) {
        //1.减少库存,即更新库存
        boolean success=itemService.reduceItem(itemVo);//考虑减少库存失败的时候，不进行写入订单
        System.out.println("秒杀结果"+success);
        System.out.println("itemVo "+itemVo.getId()+"  "+itemVo.getItemName());
        if(success) {
            //2.下订单,其中有两个订单: order_info   miaosha_order
            OrderInfo orderinfo=orderService.createOrder_Cache(user, itemVo);
            return orderinfo;
        }else {//减少库存失败
            //做一个标记，代表商品已经秒杀完了。
            setGoodsOver(itemVo.getId());
            return null;
        }
    }


    /**
     * 获取秒杀结果
     * 成功返回id
     * 失败返回0或-1
     * 0代表排队中
     * -1代表库存不足
     * @param userId
     * @param itemId
     * @return
     */
    @Override
    public long getMiaoshaResult(Long userId, long itemId) {
        SeckillOrder order=orderService.getSeckillOrderByUserIdAndItemId_Cache(userId, itemId);
        //秒杀成功
        if(order!=null) {
            System.out.println("!!@普通orderId"+order.getOrderId());
            System.out.println("!!@orderId:"+order.getId());
            return order.getOrderId();
        }
        else {
            //查看商品是否卖完了
            boolean isOver=getGoodsOver(itemId);
            if(isOver) {//商品卖完了
                return -1;
            }else {		//商品没有卖完
                return 0;
            }
        }
    }

    /**
     * 5-22
     * 先写入缓存
     *
     */
    private void setGoodsOver(Long itemId) {
        redisService.set(SeckillKey.isGoodsOver, ""+itemId, true);
    }

    /**
     * 5-22
     *
     */
    private boolean getGoodsOver(Long itemId) {
        return redisService.exitsKey(SeckillKey.isGoodsOver, ""+itemId);
    }
    /**
     * redisService.set(MiaoshaKey.getMiaoshaPath, ""+user.getId()+"_"+itemId, str);
     * 去缓存里面检查path是否正确，验证path。
     */
    public boolean checkPath(SeckillUser user, long itemId, String path) {
        if(user==null||path==null) {
            return false;
        }
        String pathRedis=redisService.get(SeckillKey.getSeckillPath, ""+user.getId()+"_"+itemId, String.class);
        return path.equals(pathRedis);
    }
    /**
     * 生成一个秒杀path，写入缓存，并且，返回至前台
     */
    @Override
    public String createMiaoshaPath(SeckillUser user, Long itemId) {
        String str= MD5Util.md5(UUIDUtil.uuid()+"123456");
        //将随机串保存在客户端，并且返回至客户端。
        //String path=""+user.getId()+"_"+itemId;
        redisService.set(SeckillKey.getSeckillPath, ""+user.getId()+"_"+itemId, str);
        return str;
    }

    public BufferedImage createMiaoshaVertifyCode(SeckillUser user, Long itemId) {
        if(user==null||itemId<=0) {
            return null;
        }
        int width=80;
        int height=30;
        BufferedImage img=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics g=img.getGraphics();
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, width-1, height-1);
        Random rdm=new Random();
        for(int i=0;i<50;i++) {
            int x=rdm.nextInt(width);
            int y=rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        //生成验证码
        String vertifyCode=createVertifyCode(rdm);
        g.setColor(new Color(0,100,0));
        g.setFont(new Font("Candara",Font.BOLD,24));
        //将验证码写在图片上
        g.drawString(vertifyCode, 8, 24);
        g.dispose();
        //计算存值
        int rnd=calc(vertifyCode);
        //将计算结果保存到redis上面去
        redisService.set(SeckillKey.getSeckillVertifyCode, ""+user.getId()+"_"+itemId, rnd);
        return img;
    }

    private static int calc(String exp) {
        try {
            ScriptEngineManager manager=new ScriptEngineManager();
            ScriptEngine engine=manager.getEngineByName("JavaScript");
            return (Integer) engine.eval(exp);
        }catch(Exception e){
            e.printStackTrace();
            return 0;
        }
    }
    private static char[]ops=new char[] {'+','-','*'};
    /**
     * + - *
     */
    private String createVertifyCode(Random rdm) {
        //生成10以内的
        int n1=rdm.nextInt(10);
        int n2=rdm.nextInt(10);
        int n3=rdm.nextInt(10);
        char op1=ops[rdm.nextInt(3)];//0  1  2
        char op2=ops[rdm.nextInt(3)];//0  1  2
        String exp=""+n1+op1+n2+op2+n3;
        return exp;
    }
    /**
     * 验证验证码，取缓存里面取得值，验证是否相等
     */
    public boolean checkVCode(SeckillUser user, Long itemId, int vertifyCode) {
        Integer redisVCode=redisService.get(SeckillKey.getSeckillVertifyCode, user.getId()+"_"+itemId, Integer.class);
        if(redisVCode==null||redisVCode-vertifyCode!=0) {
            return false;
        }
        //删除缓存里面的数据
        redisService.delete(SeckillKey.getSeckillVertifyCode, user.getId()+"_"+itemId);
        return true;
    }
}
