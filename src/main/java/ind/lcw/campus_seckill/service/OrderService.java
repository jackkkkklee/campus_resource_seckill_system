package ind.lcw.campus_seckill.service;

import ind.lcw.campus_seckill.entity.OrderInfo;
import ind.lcw.campus_seckill.entity.SeckillOrder;
import ind.lcw.campus_seckill.entity.SeckillUser;
import ind.lcw.campus_seckill.vo.ItemVo;

import java.util.List;

public interface OrderService {
    // 查询是否有秒杀订单
    SeckillOrder getSeckillOrderByUserIdAndItemId_Cache(Long userId, Long itemId);

    //生成秒杀订单
    OrderInfo createOrder_Cache(SeckillUser user, ItemVo itemVo);
    OrderInfo getOrderByOrderId(long orderId);


    //获取用户所有的订单信息
    List<OrderInfo> getAllOrderByUserId(Long id);
    //修改订单状态为已确认
    Boolean updateOrderByStatus(Integer orderStatus,Long id);
}
