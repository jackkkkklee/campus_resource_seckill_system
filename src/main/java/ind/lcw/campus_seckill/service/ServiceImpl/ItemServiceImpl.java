package ind.lcw.campus_seckill.service.ServiceImpl;


import ind.lcw.campus_seckill.entity.SeckillItem;
import ind.lcw.campus_seckill.service.ItemService;
import ind.lcw.campus_seckill.vo.ItemVo;
import ind.lcw.campus_seckill.mapper.ItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ItemServiceImpl implements ItemService {
    // 对于item这种不会频繁访问的就不设置缓存了
    @Autowired
    ItemMapper itemMapper;
    @Override
    public List<ItemVo> getItemVoList() {

        return itemMapper.getItemVoList();
    }

    @Override
    public ItemVo getItemVoByItemId(Long id) {
        return itemMapper.getItemVoById(id);
    }

    /**
     * 如果库存不足直接返回，为了避免并发问题，两次判断
     * @param itemVo
     * @return
     */
    @Override
    public Boolean reduceItem(ItemVo itemVo) {

        int res= itemMapper.reduceStock(itemVo.getId());
        System.out.println("res "+res);
        System.out.println("sql语句 "+"id=itemVo.getId() "+"update seckill_item set stock_count=stock_count-1 where item_id=#{itemId} and stock_count>0");
        return res>0;


    }
}
