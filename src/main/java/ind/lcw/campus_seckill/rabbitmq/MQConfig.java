package ind.lcw.campus_seckill.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

//作用 声明当前类是一个配置类,相当于一个Spring的XML配置文件,与@Bean配
//@Configuration标注在类上，相当于把该类作为spring的xml配置文件中的<beans>，作用为：配置spring容器(应用上下文)
@Configuration
public class MQConfig {
	public static final String SECKILL_QUEUE = "SECKILL.queue";
	public static final String User_EXCHANGE = "User.Exchange";
	public static final String DEAD_COMMON_EXCHANGE = "Dead.common";
	public static final String XDL_QUEUE_User = "Dead.User";
	/**
	 * Direct模式，交换机Exchange:
	 * 发送者，将消息往外面发送的时候，并不是直接投递到队列里面去，而是先发送到交换机上面，然后由交换机发送数据到queue上面去，
	 * 做了一次路由。
	 *
	@Bean
	public Queue queue() {
		//名称，是否持久化
		return new Queue(QUEUE, true);
	}

	@Bean
	public Queue SECKILLqueue() {
		//名称，是否持久化
		return new Queue(SECKILL_QUEUE, true);
	}
	**/
	@Bean
	public Queue userQueue() {
		return QueueBuilder
				.durable(SECKILL_QUEUE)
				//声明该队列的死信消息发送到的 交换机 （队列添加了这个参数之后会自动与该交换机绑定，并设置路由键，不需要开发者手动设置)
				.withArgument("x-dead-letter-exchange", DEAD_COMMON_EXCHANGE)
				//声明该队列死信消息在交换机的 路由键
				.withArgument("x-dead-letter-routing-key", "user-dead-letter-routing-key")
				.withArgument("x-message-ttl", 500000)// 消息过期时间后送入死信队列
				.build();
	}


	@Bean
	public Exchange userExchange() {
		return ExchangeBuilder
				.topicExchange(User_EXCHANGE)
				.durable(true)
				.build();
	}

	/**
	 * 用户队列与交换机绑定
	 *
	 * @param userQueue    用户队列名
	 * @param userExchange 用户交换机名
	 * @return
	 */
	@Bean
	public Binding userBinding(Queue userQueue, Exchange userExchange) {
		return BindingBuilder
				.bind(userQueue)
				.to(userExchange)
				.with("user.*")
				.noargs();
	}


	@Bean
	public Exchange XDL_QUEUE() {
		return ExchangeBuilder
				.topicExchange(DEAD_COMMON_EXCHANGE)
				.durable(true)
				.build();
	}


	/**
	 * 用户队列的死信消息 路由的队列
	 * 用户队列user-queue的死信投递到死信交换机`common-dead-letter-exchange`后再投递到该队列
	 * 用这个队列来接收user-queue的死信消息
	 *
	 * @return
	 */
	@Bean
	public Queue XDL_QUEUE_User() {

		return QueueBuilder
				.durable(XDL_QUEUE_User)
				.build();

	}

	/**
	 * 死信队列绑定死信交换机
	 *
	 * @param XDL_QUEUE_User      user-queue对应的死信队列
	 * @param XDL_QUEUE 通用死信交换机
	 * @return
	 */
	@Bean
	public Binding userDeadLetterBinding(Queue XDL_QUEUE_User, Exchange XDL_QUEUE) {
		return BindingBuilder
				.bind(XDL_QUEUE_User)
				.to(XDL_QUEUE)
				.with("user-dead-letter-routing-key")
				.noargs();
	}

}




