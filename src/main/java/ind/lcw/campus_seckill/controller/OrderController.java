package ind.lcw.campus_seckill.controller;

import ind.lcw.campus_seckill.entity.OrderInfo;
import ind.lcw.campus_seckill.entity.SeckillUser;
import ind.lcw.campus_seckill.redis.RedisService;
import ind.lcw.campus_seckill.result.CodeMsg;
import ind.lcw.campus_seckill.result.Result;
import ind.lcw.campus_seckill.service.ItemService;
import ind.lcw.campus_seckill.service.OrderService;
import ind.lcw.campus_seckill.service.SeckillUserService;
import ind.lcw.campus_seckill.vo.ItemVo;
import ind.lcw.campus_seckill.vo.OrderDetailVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/order")
@Controller
public class OrderController {
    @Autowired
    ItemService itemService;
    @Autowired
    RedisService redisService;
    @Autowired
    SeckillUserService seckillUserService;

    @Autowired
    OrderService orderService;

    @RequestMapping("/detail")
    @ResponseBody
    //@NeedLogin
    /**
     * 		@NeedLogin使用一个拦截器，不用每次都去判断user是否为空，在拦截器里面user为空，直接返回某页面。
     * @param model
     * @param user
     * @param orderId
     * @return
     */
    public Result<OrderDetailVo> info(Model model, SeckillUser user,
                                      @RequestParam("orderId") long orderId) {
        if(user==null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        OrderInfo order=orderService.getOrderByOrderId(orderId);
        if(order==null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        //订单存在的情况
        long ItemId=order.getItemId();
        ItemVo gVo=itemService.getItemVoByItemId(ItemId);
        OrderDetailVo oVo=new OrderDetailVo();
        oVo.setItemVo(gVo);
        oVo.setOrder(order);
        return Result.success(oVo);//返回页面login
    }
    @ApiOperation("返回订单voList")
    @RequestMapping("/get_user_order")
    public String getOrderDetail(SeckillUser user,Model model){
        List<OrderDetailVo> orderDetailVoList=new ArrayList<>();
        //获取用户的所有orderId
        List<OrderInfo> orderInfoList=orderService.getAllOrderByUserId(user.getId());

        for(OrderInfo orderInfo:orderInfoList){
            long ItemId=orderInfo.getItemId();
            ItemVo gVo=itemService.getItemVoByItemId(ItemId);

            OrderDetailVo orderDetailVo=new OrderDetailVo();
            orderDetailVo.setItemVo(gVo);
            orderDetailVo.setOrder(orderInfo);

            orderDetailVoList.add(orderDetailVo);
        }
        model.addAttribute("orderDetailList",orderDetailVoList);
        return "user_order_info";
    }


}
