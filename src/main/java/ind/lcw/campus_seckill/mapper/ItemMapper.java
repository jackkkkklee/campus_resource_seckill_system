package ind.lcw.campus_seckill.mapper;

import ind.lcw.campus_seckill.entity.SeckillItem;
import ind.lcw.campus_seckill.vo.ItemVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
@Mapper
public interface ItemMapper {
    @Select("select i.*,si.stock_count,si.start_date,si.end_date from seckill_item si left join item i on si.item_id=i.id where i.id=#{id}")
    ItemVo getItemVoById(@Param("id") Long id);
    @Select("select i.*,si.stock_count,si.start_date,si.end_date from seckill_item si left join item i on si.item_id=i.id")
    public List<ItemVo> getItemVoList();
    @Update("update seckill_item set stock_count=stock_count-1 where item_id=#{itemId} and stock_count>0")
    int reduceStock(Long itemId);
}
