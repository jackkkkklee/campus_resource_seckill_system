package ind.lcw.campus_seckill.controller;

import ind.lcw.campus_seckill.entity.SeckillUser;
import ind.lcw.campus_seckill.rabbitmq.SeckillMessage;
import ind.lcw.campus_seckill.redis.SeckillKey;
import ind.lcw.campus_seckill.vo.ItemVo;
import ind.lcw.campus_seckill.entity.SeckillOrder;
import ind.lcw.campus_seckill.rabbitmq.MQSender;
import ind.lcw.campus_seckill.redis.AccessKey;
import ind.lcw.campus_seckill.redis.GoodsKey;
import ind.lcw.campus_seckill.redis.RedisService;
import ind.lcw.campus_seckill.result.CodeMsg;
import ind.lcw.campus_seckill.result.Result;
import ind.lcw.campus_seckill.service.ItemService;
import ind.lcw.campus_seckill.service.OrderService;
import ind.lcw.campus_seckill.service.SeckillService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@RequestMapping("/seckill")
@Controller
public class SeckillController implements InitializingBean {
    @Autowired
    ItemService itemService;
    @Autowired
    RedisService redisService;
    @Autowired
    SeckillService seckillService;
    @Autowired
    OrderService orderService;
    @Autowired
    MQSender mqSender;
    //缓存设置库存
    /**
     * 系统初始化的时候做的事情。
     * 在容器启动时候，检测到了实现了接口InitializingBean之后，
     * 预加载库存
     */
    //可以用来标记库存结束，减少redis的访问，这里暂时不用
    // Map<Long,Boolean> localMap=new HashMap<Long,Boolean>();
    @Override
    public void afterPropertiesSet() throws Exception {
        List<ItemVo> Itemlist= itemService.getItemVoList();
        if(Itemlist==null) {
            return;
        }
        for(ItemVo Item:Itemlist) {
            //如果不是null的时候，将库存加载到redis里面去 prefix---ItemKey:gs ,	 key---商品id,	 value
            redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+Item.getId(), Item.getStockCount());
        }
    }

    /**
     * 秒杀的策略是：
     * 1.缓存预热，加载库存
     * 2.请求时预减库存
     * 3.入队
     * 4.出队进行SeckillService中事务性原子的减少库存，创建订单操作
     * 5.客户端可以去询问进度
     * @param model
     * @param user
     * @param itemId
     * @param path
     * @return
     */
    @RequestMapping(value="/{path}/do_seckill_ajaxcache",method= RequestMethod.POST)
    @ResponseBody
    public Result<Integer> doSeckillCache(Model model, SeckillUser user,
                                          @RequestParam(value="itemId",defaultValue="0") long itemId,
                                          @PathVariable("path")String path) {

        //有以下几个逻辑需要判断
        /*
        1.用户是否登录
        2.访问的路径是否正确
        3.用户是否多次下单
         */
        if(user==null)
            return Result.error(CodeMsg.SESSION_ERROR);
        model.addAttribute("user",user);
        if(!seckillService.checkPath(user,itemId,path)){
            return Result.error(CodeMsg.REQUEST_ILLEAGAL);
        }

        SeckillOrder order=orderService.getSeckillOrderByUserIdAndItemId_Cache(user.getId(),itemId);
        if(order!=null){ //已经有订单生成了
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //判断-1后的库存是否大于等于0
        //这里采取的是预先减少redis内的库存，拦截大量无效请求
        //但是还在队列中的用户，再次发送请求，内存库存会减少（虽然在队列消息接收者中会判断所以不会超卖，但是变相减少了其他活动参与者）
        //所以在redis层面添加订单重复的判断
        //同时要考虑redis出现问题的情况，以及消息消费失败等异常
        boolean b = redisService.set(SeckillKey.isRedisDre, "" + user.getId() + "_" + itemId, true);
        if (!b) {
            log.debug(user.getId() + "已下单" + itemId + "需等待结果");
            return Result.error(CodeMsg.MIAOSHA_REPEATE_ORDER);
        }
        //先判断，再减少，再并发环境下会有多余的用户发送消息到队列  如果能用lua原子化就可以
        Long stock=redisService.decr(GoodsKey.getMiaoshaGoodsStock,""+itemId);
        if(stock<0){
            //考虑到有订单取消，这里要回补，保持最终一致.不然有多500个人访问， redis的库存会是-500
            redisService.incr(GoodsKey.getMiaoshaGoodsStock, "" + itemId);
            return Result.error(CodeMsg.MIAOSHA_OVER_ERROR);
        }

        //如果都没有问题，就发送秒杀到消息到消息队列
        SeckillMessage seckillMessage=new SeckillMessage();
        seckillMessage.setItemId(itemId);
        seckillMessage.setUser(user);
        mqSender.sendSeckillMessage(seckillMessage);
        return Result.success(0);//处理中

    }

    //生成验证码
    /**
     * 生成图片验证码
     */
    @RequestMapping(value ="/vertifyCode")
    @ResponseBody
    public Result<String> getVertifyCode(Model model, SeckillUser user,
                                         @RequestParam("itemId") Long itemId, HttpServletResponse response) {
        model.addAttribute("user", user);
        //如果用户为空，则返回至登录页面
        if(user==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        BufferedImage img=seckillService.createMiaoshaVertifyCode(user, itemId);
        try {
            OutputStream out=response.getOutputStream();
            ImageIO.write(img,"JPEG", out);
            out.flush();
            out.close();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }

    //验证码验证，并获取秒杀路径
    /**
     * 获取秒杀的path,并且验证验证码的值是否正确
     */
    //@AccessLimit(seconds=5,maxCount=5,needLogin=true)
    //加入注解，实现拦截功能，进而实现限流功能
    //@AccessLimit(seconds=5,maxCount=5,needLogin=true)
    @RequestMapping(value ="/getPath")
    @ResponseBody
    public Result<String> getSeckillPath(HttpServletRequest request, Model model, SeckillUser user,
                                         @RequestParam("itemId") Long itemId,
                                         @RequestParam(value="vertifyCode",defaultValue="0") int vertifyCode) {
        model.addAttribute("user", user);
        //如果用户为空，则返回至登录页面
        if(user==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //限制访问次数
        String uri=request.getRequestURI();
        String key=uri+"_"+user.getId();
        //限定key5s之内只能访问5次
        Integer count=redisService.get(AccessKey.access, key, Integer.class);
        if(count==null) {
            redisService.set(AccessKey.access, key, 1);
        }else if(count<5) {
            redisService.incr(AccessKey.access, key);
        }else {//超过5次
            return Result.error(CodeMsg.ACCESS_LIMIT);
        }
        //验证验证码
        boolean check=seckillService.checkVCode(user, itemId,vertifyCode );
        if(!check) {
            return Result.error(CodeMsg.REQUEST_ILLEAGAL);
        }
        System.out.println("通过!");
        //生成一个随机串
        String path=seckillService.createMiaoshaPath(user,itemId);
        System.out.println("@SeckillController-toSeckillPath-path:"+path);
        return Result.success(path);
    }
    //获取秒杀结果
    /**
     * 客户端做一个轮询，查看是否成功与失败，失败了则不用继续轮询。
     * 秒杀成功，返回订单的Id。
     * 库存不足直接返回-1。
     * 排队中则返回0。
     * 查看是否生成秒杀订单。
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> doSeckillResult(Model model, SeckillUser user,
                                        @RequestParam(value = "itemId", defaultValue = "0") long itemId) {
        long result=seckillService.getMiaoshaResult(user.getId(),itemId);
        System.out.println("轮询 result："+result);
        return Result.success(result);
    }

    @ResponseBody
    @RequestMapping(value = "/info" ,method= RequestMethod.POST)
    public Result<SeckillUser> getUserInfo(SeckillUser user){
        return Result.success(user);
    }


}
