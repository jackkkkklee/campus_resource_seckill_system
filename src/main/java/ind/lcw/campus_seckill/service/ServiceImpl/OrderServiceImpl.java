package ind.lcw.campus_seckill.service.ServiceImpl;

import ind.lcw.campus_seckill.entity.OrderInfo;
import ind.lcw.campus_seckill.entity.SeckillOrder;
import ind.lcw.campus_seckill.entity.SeckillUser;
import ind.lcw.campus_seckill.redis.RedisService;
import ind.lcw.campus_seckill.vo.ItemVo;
import ind.lcw.campus_seckill.mapper.OrderMapper;
import ind.lcw.campus_seckill.redis.OrderKey;
import ind.lcw.campus_seckill.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    RedisService redisService;
    @Autowired
    OrderMapper orderMapper;

    /**
     * 从缓存中拿订单，没有返回null
     * @param userId
     * @param itemId
     * @return
     */
    @Override
    public SeckillOrder getSeckillOrderByUserIdAndItemId_Cache(Long userId, Long itemId) {
        SeckillOrder seckillOrder=redisService.get(OrderKey.getMiaoshaOrderByUidAndGid,""+userId+"_"+itemId,SeckillOrder.class);
        return seckillOrder;
    }

    /**
     * 用事务 新建订单详情，设置秒杀订单，将秒杀订单放入缓存
     * @param user
     * @param itemVo
     * @return
     */
    @Transactional
    @Override
    public OrderInfo createOrder_Cache(SeckillUser user, ItemVo itemVo) {
        OrderInfo orderInfo=new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setItemId(itemVo.getId());
        orderInfo.setUserId(user.getId());
        orderInfo.setItemCount(1);
        orderInfo.setOrderChannel(1);
        orderInfo.setOrderStatus(0);// 订单状态只有新建 ， 已经确认，取消
        orderMapper.insert(orderInfo);
        //创建秒杀订单
        SeckillOrder seckillOrder=new SeckillOrder();
        seckillOrder.setOrderId(orderInfo.getId());
        System.out.println("订单id"+seckillOrder.getOrderId());
        seckillOrder.setItemId(itemVo.getId());
        seckillOrder.setUserId(user.getId());
        //将秒杀订单放入缓存
        redisService.set(OrderKey.getMiaoshaOrderByUidAndGid,""+user.getId()+"_"+itemVo.getId(),seckillOrder);
        return orderInfo;

    }
    @Override
    public OrderInfo getOrderByOrderId(long orderId){
        return orderMapper.getOrderByOrderId(orderId);
    }

    @Override
    public List<OrderInfo> getAllOrderByUserId(Long id) {
        return orderMapper.selectAllOrderInfoByUserId(id);
    }

    @Override
    public Boolean updateOrderByStatus(Integer orderStatus, Long id) {
        return orderMapper.updateOrderByStatusAndId(2,id);
    }


}
