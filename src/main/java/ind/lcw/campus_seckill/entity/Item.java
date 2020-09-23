package ind.lcw.campus_seckill.entity;

import lombok.Data;

@Data
public class Item {
    private Long id;
    private String itemName;
    private String itemTitle;
    private String itemImg;
    private String itemDetail;
    private Integer itemStock;
    private String itemRequirement; // item的具体要求
}
