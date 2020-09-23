package ind.lcw.campus_seckill.rabbitmq;


import com.alibaba.fastjson.JSON;
import ind.lcw.campus_seckill.entity.SeckillOrder;
import ind.lcw.campus_seckill.entity.SeckillUser;
import ind.lcw.campus_seckill.redis.RedisService;
import ind.lcw.campus_seckill.vo.ItemVo;
import ind.lcw.campus_seckill.service.OrderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

//接收者
@Service
@Log4j2
public class MQReceiver {
	@Autowired
	ind.lcw.campus_seckill.service.ItemService ItemService;
	@Autowired
	RedisService redisService;
	@Autowired
	ind.lcw.campus_seckill.service.SeckillUserService SeckillUserService;
	//作为秒杀功能事务的Service
	@Autowired
	ind.lcw.campus_seckill.service.SeckillService SeckillService;
	@Autowired
	OrderService orderService;



	@RabbitListener(queues=MQConfig.SECKILL_QUEUE)//指明监听的是哪一个queue
	public void receiveSeckill(String message) {
		log.info("receiveSeckill message:"+message);
		//通过string类型的message还原成bean
		//拿到了秒杀信息之后。开始业务逻辑秒杀，
		SeckillMessage mm=RedisService.stringToBean(message, SeckillMessage.class);
		SeckillUser user=mm.getUser();
		long ItemId=mm.getItemId();
		ItemVo itemVo=ItemService.getItemVoByItemId(ItemId);
		System.out.println(itemVo);
		if(itemVo==null)
			return ;
		int  stockcount=itemVo.getStockCount();
		//1.判断库存不足
		if(stockcount<=0) {//失败			库存至临界值1的时候，此时刚好来了加入10个线程，那么库存就会-10
			//model.addAttribute("errorMessage", CodeMsg.Seckill_OVER_ERROR);
			return;
		}
		//2.判断这个秒杀订单形成没有，判断是否已经秒杀到了，避免一个账户秒杀多个商品
		SeckillOrder order = orderService.getSeckillOrderByUserIdAndItemId_Cache(user.getId(), ItemId);
		if (order != null) {// 重复下单
			// model.addAttribute("errorMessage", CodeMsg.REPEATE_Seckill);
			return;
		}
		//原子操作：1.库存减1，2.下订单，3.写入秒杀订单--->是一个事务
		//SeckillService.Seckill(user,Itemvo);
		SeckillService.miaosha1(user,itemVo);

	}
	@RabbitListener(queues = MQConfig.XDL_QUEUE_User)
	public void userDeadLetterConsumer(String message) {
		log.info("接收到死信消息:[{}]", message);
	}




//	@RabbitListener(queues=MQConfig.QUEUE)//指明监听的是哪一个queue
//	public void receive(String message) {
//		log.info("receive message:"+message);
//	}
//
//	@RabbitListener(queues=MQConfig.TOPIC_QUEUE1)//指明监听的是哪一个queue
//	public void receiveTopic1(String message) {
//		log.info("receiveTopic1 message:"+message);
//	}
//
//	@RabbitListener(queues=MQConfig.TOPIC_QUEUE2)//指明监听的是哪一个queue
//	public void receiveTopic2(String message) {
//		log.info("receiveTopic2 message:"+message);
//	}
//
//	@RabbitListener(queues=MQConfig.HEADER_QUEUE)//指明监听的是哪一个queue
//	public void receiveHeaderQueue(byte[] message) {
//		log.info("receive Header Queue message:"+new String(message));
//	}
}
