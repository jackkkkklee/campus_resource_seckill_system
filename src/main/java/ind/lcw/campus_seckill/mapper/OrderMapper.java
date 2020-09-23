package ind.lcw.campus_seckill.mapper;

import ind.lcw.campus_seckill.entity.OrderInfo;
import ind.lcw.campus_seckill.entity.SeckillOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;


@Mapper
public interface OrderMapper {
    @Select("select * from seckill_order where user_id=#{userId} and item_id=#{itemId}")
    public SeckillOrder getseckillOrderByUserIdAndItemId(@Param("userId") Long userId, @Param("itemId") Long itemId);
    @Insert("insert into order_info (user_id,item_id,item_name,item_count,order_channel,order_status,create_date) values "
            + "(#{userId},#{itemId},#{itemName},#{itemCount},#{orderChannel},#{orderStatus},#{createDate})")
    @SelectKey(keyColumn="id",keyProperty="id",resultType=long.class,before=false,statement="select last_insert_id()")
    public long insert(OrderInfo orderInfo);//使用注解获取返回值，返回上一次插入的id

    @Select("select * from order_info where user_id=#{userId} and item_id=#{itemId}")
    public OrderInfo selectorderInfo(@Param("userId") Long userId, @Param("itemId") Long itemId);//使用注解获取返回值，返回上一次插入的id

    @Insert("insert into seckill_order (user_id,item_id,order_id) values (#{userId},#{itemId},#{orderId})")
    public void insertseckillOrder(SeckillOrder seckillorder);

    @Select("select * from order_info where id=#{orderId}")
    public OrderInfo getOrderByOrderId(@Param("orderId") long orderId);

    @Select("select * from order_info where user_id=#{userId} ")
    public List<OrderInfo> selectAllOrderInfoByUserId(@Param("userId") Long userId);

    @Update("update  order_info set orderStatus = #{orderStatus} where id=#{id}")
    public Boolean updateOrderByStatusAndId(Integer orderStatus,Long id);

}
