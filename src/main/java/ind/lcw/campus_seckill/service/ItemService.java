package ind.lcw.campus_seckill.service;

import ind.lcw.campus_seckill.vo.ItemVo;

import java.util.List;

public interface ItemService {
    List<ItemVo> getItemVoList();
    ItemVo getItemVoByItemId(Long id);
    Boolean reduceItem(ItemVo itemVo);
}
