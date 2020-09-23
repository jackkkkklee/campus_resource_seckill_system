package ind.lcw.campus_seckill.vo;

import ind.lcw.campus_seckill.entity.SeckillUser;
import lombok.Data;

@Data
public class ItemDetailVo {
    // 秒杀状态量
    private int status = 0;
    // 开始时间倒计时
    private int remailSeconds = 0;
     ItemVo itemVo;
    private SeckillUser user;
}
