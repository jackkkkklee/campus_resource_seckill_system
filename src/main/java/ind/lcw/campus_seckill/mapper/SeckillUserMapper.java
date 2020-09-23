package ind.lcw.campus_seckill.mapper;

import ind.lcw.campus_seckill.entity.SeckillUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
@Mapper
public interface SeckillUserMapper {
    @Select("select * from seckill_user where id=#{id}")  //这里#{id}通过后面参数来为其赋值
    public SeckillUser getById(@Param("id") long id);    //绑定

    //绑定在对象上面了----@Param("id")long id,@Param("pwd")long pwd 效果一致
    @Update("update seckill_user set pwd=#{pwd} where id=#{id}")
    public void update(SeckillUser toupdateuser);
}
