package ind.lcw.campus_seckill.entity;

import lombok.Data;
import sun.rmi.runtime.Log;

import java.util.Date;


@Data
public class OrderInfo {
    private Long id;
    private Long userId;
    private Long itemId;
    private String itemName;
    private Integer itemCount;
    private Integer orderChannel;
    private Integer orderStatus;
    private Date createDate;
    private Date confirmationDate;
}
