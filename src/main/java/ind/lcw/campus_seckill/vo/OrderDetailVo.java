package ind.lcw.campus_seckill.vo;

import ind.lcw.campus_seckill.entity.OrderInfo;
import lombok.Data;

@Data
public class OrderDetailVo {
    private ItemVo ItemVo;
    private OrderInfo order;

}
