package ind.lcw.campus_seckill.vo;

import ind.lcw.campus_seckill.entity.Item;
import lombok.Data;

import java.sql.Date;
@Data
public class ItemVo extends Item {

    private Date startDate;
    private Date endDate;
    private Integer stockCount;
}
