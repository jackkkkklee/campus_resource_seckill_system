package ind.lcw.campus_seckill.service;

import ind.lcw.campus_seckill.entity.OrderInfo;
import ind.lcw.campus_seckill.entity.SeckillUser;
import ind.lcw.campus_seckill.vo.ItemVo;

import java.awt.image.BufferedImage;

public interface SeckillService {
    OrderInfo miaosha1(SeckillUser user, ItemVo itemVo);//秒杀服务逻辑
     long getMiaoshaResult(Long userId, long ItemId);//获取结果

    //生产随机的访问路径
    String createMiaoshaPath(SeckillUser user, Long goodsId);
    boolean checkPath(SeckillUser user, long goodsId, String path);


    //验证码服务
     BufferedImage createMiaoshaVertifyCode(SeckillUser user, Long goodsId);
    boolean checkVCode(SeckillUser user, Long goodsId, int vertifyCode);

}
