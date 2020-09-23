package ind.lcw.campus_seckill.entity;

import lombok.Data;

@Data
public class SeckillOrder {
    private Long id;
    private Long userId;
    private Long orderId;
    private Long itemId;
}
