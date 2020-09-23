package ind.lcw.campus_seckill.controller;

import ind.lcw.campus_seckill.entity.SeckillUser;
import ind.lcw.campus_seckill.result.CodeMsg;
import ind.lcw.campus_seckill.result.Result;
import ind.lcw.campus_seckill.service.ItemService;
import ind.lcw.campus_seckill.vo.ItemVo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
@Log4j2
@RequestMapping("/user")
@Controller
public class UserController {
    //返回用户info
    @Autowired
    ItemService itemService;
    @RequestMapping("/info")
    @ResponseBody
    Result<SeckillUser> userinfo(SeckillUser user){
        return Result.success(user);
    }
    //用户访问秒杀详情页面
    @ApiOperation("用户访问秒杀详情页")
    @RequestMapping("/to_detail/{itemId}")
    public String toDetail(Model model, SeckillUser user, @PathVariable("itemId")long itemId) {//id一般用snowflake算法
        model.addAttribute("user", user);
        ItemVo item= itemService.getItemVoByItemId(itemId);
        model.addAttribute("item", item);
        //既然是秒杀，还要传入秒杀开始时间，结束时间等信息
        long start=item.getStartDate().getTime();
        long end=item.getEndDate().getTime();
        long now=System.currentTimeMillis();
        //秒杀状态量
        int status=0;
        //开始时间倒计时
        int remailSeconds=0;
        //查看当前秒杀状态
        if(now<start) {//秒杀还未开始，--->倒计时
            status=0;
            remailSeconds=(int) ((start-now)/1000);  //毫秒转为秒
        }else if(now>end){ //秒杀已经结束
            status=2;
            remailSeconds=-1;  //毫秒转为秒
        }else {//秒杀正在进行
            status=1;
            remailSeconds=0;  //毫秒转为秒
        }
        model.addAttribute("status", status);
        model.addAttribute("remailSeconds", remailSeconds);
        return "item_detail";//返回页面login
    }

    @RequestMapping("/get_user_info")
    public String getUserInfo(SeckillUser user,Model model){
        model.addAttribute("user",user);
        return "/user_info";
    }

}

