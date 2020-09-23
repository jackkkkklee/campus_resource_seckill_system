package ind.lcw.campus_seckill.entity;

import lombok.Data;

import java.util.Date;


@Data
public class SeckillItem {
    private Long id;
    private Long  itemId;// 1 对 1
    private Date startDate;
    private Date endDate;
    private Integer stockCount;// 虽然默认是和普通库存数量一样
}
