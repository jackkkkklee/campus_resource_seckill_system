package ind.lcw.campus_seckill.controller;

import ind.lcw.campus_seckill.entity.SeckillUser;
import ind.lcw.campus_seckill.redis.RedisService;
import ind.lcw.campus_seckill.result.CodeMsg;
import ind.lcw.campus_seckill.result.Result;
import ind.lcw.campus_seckill.service.OrderService;
import ind.lcw.campus_seckill.service.UserService;
import ind.lcw.campus_seckill.vo.LoginVo;
import ind.lcw.campus_seckill.service.SeckillUserService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.validation.Valid;

import javax.servlet.http.HttpServletResponse;
@Log4j2
@Controller
@RequestMapping("/login")
public class LoginController {
    @Autowired
    UserService userService;
    @Autowired
    RedisService redisService;
    @Autowired
    SeckillUserService miaoshaUserService;
    @Autowired
    OrderService orderService;
    //返回自定义的Result格式

    //日志
    //返回登录页
    @ApiOperation("返回登录页")
    @RequestMapping("/to_login")
    public String toLogin() {
        return "login";//返回页面login
    }
    //通过参数返回登录
    //使用JSR303校验
    @ApiOperation("进行登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name="response",value="响应对象",dataType = "HttpServletResponse"),
            @ApiImplicitParam(name="login",value="登录vo",dataType = "LoginVo")})
    @RequestMapping("/do_login")//作为异步操作
    @ResponseBody
    public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {//0代表成功
        CodeMsg m=miaoshaUserService.login(response,loginVo);
        if(m.getCode()==0)
            return Result.success(true);
        else
            return Result.error(m);
    }

    @ApiOperation("返回活动列表页")
    @RequestMapping("/to_list")
    @ResponseBody
    public Result<Boolean> toList(SeckillUser user) {
        if(user!=null)
            return Result.success(true);
       else
           return Result.error(CodeMsg.REQUEST_ILLEAGAL);
    }
    @RequestMapping("/to_index")
    public String toIndex(){
        return "index";
    }
    @RequestMapping("/to_index_with_confirm")
    public String toIndexWithConfirm(Long orderId){
        orderService.updateOrderByStatus(2,orderId);
        return "index";
    }


}
