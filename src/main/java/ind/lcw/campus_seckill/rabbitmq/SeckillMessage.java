package ind.lcw.campus_seckill.rabbitmq;


import ind.lcw.campus_seckill.entity.SeckillUser;
import lombok.Data;

@Data
public class SeckillMessage {
	private SeckillUser user;
	private long itemId;


}
