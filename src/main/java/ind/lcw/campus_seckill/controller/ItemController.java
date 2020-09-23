package ind.lcw.campus_seckill.controller;

import ind.lcw.campus_seckill.entity.SeckillUser;
import ind.lcw.campus_seckill.redis.GoodsKey;
import ind.lcw.campus_seckill.redis.RedisService;
import ind.lcw.campus_seckill.result.Result;
import ind.lcw.campus_seckill.service.ItemService;
import ind.lcw.campus_seckill.service.OrderService;
import ind.lcw.campus_seckill.vo.ItemDetailVo;
import ind.lcw.campus_seckill.vo.ItemVo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Log4j2
@Controller
@RequestMapping("/item")
public class ItemController {
    @Autowired
    RedisService redisService;
    @Autowired
    ItemService itemService;
    @Autowired
    OrderService orderService;
    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @RequestMapping("/to_list_noCache")
    public String toListnoCache(Model model,SeckillUser user) {
        model.addAttribute("user", user);
        //查询商品列表
        List<ItemVo> itemList= itemService.getItemVoList();
        model.addAttribute("itemList", itemList);
        return "item_list";//返回页面login
    }

    //做缓存的list页面 ，整个html页面的缓存
    @ApiOperation("对itemList页面缓存，返回")
    @RequestMapping(value="/to_list",produces="text/html")
    @ResponseBody
    public String toListCache(Model model, SeckillUser user, HttpServletRequest request,
                              HttpServletResponse response) {

        //缓存有则取
        String html=redisService.get(GoodsKey.getGoodsList,"",String.class);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        //否则获取页面，渲染并放入缓存 返回
        //查询活动列表
        List<ItemVo> itemVoList=itemService.getItemVoList();
        //将活动列表放入model
        model.addAttribute("itemList",itemVoList);

        WebContext ctx = new WebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap());
        // 手动渲染
        html = thymeleafViewResolver.getTemplateEngine().process("item_list", ctx);
        //放入缓存
        redisService.set(GoodsKey.getGoodsList,"",html);

        return html;
    }

    /**
     * 缓存活动详情，也就是单个活动的具体信息
     * @param model
     * @param user
     * @param request
     * @param response
     * @param itemId
     * @return
     */
    //做缓存的商品详情页
    @ApiOperation("活动详情页面的缓存，返回")
    @RequestMapping(value="/to_detail_html/{itemId}")  //produces="text/html"
    @ResponseBody
    public String toDetailCachehtml(Model model,SeckillUser user,
                                    HttpServletRequest request,HttpServletResponse response,@PathVariable("itemId")long itemId) {//id一般用snowflake算法
        //有缓存则取
        String html=redisService.get(GoodsKey.getGoodsDetail,"",String.class);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        ItemVo itemVo=itemService.getItemVoByItemId(itemId);
        model.addAttribute("user",user);
        model.addAttribute("itemvo",itemVo);

        //此外，秒杀页面还需要时间等参数
        int status=0;// 0 未开始 1进行中 2 结束了
        int restSecond=0;//倒计时时间
        Long start=itemVo.getStartDate().getTime();
        Long end=itemVo.getEndDate().getTime();
        Long now= System.currentTimeMillis();
        if(start<now&&now<end){
            //进行中
            status=1;
            restSecond=0;
        }else if (now>end){
            //超过活动时间
            status=2;
            restSecond=-1;
        }else{
            restSecond=(int)(start-now)/1000;
        }
        model.addAttribute("status",status);
        model.addAttribute("remailSeconds", restSecond);

        WebContext ctx = new WebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap());
        // 手动渲染
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
        return html;
    }

    /**
     * 使用页面静态化技术，传递静态的页面，利用ajax实现页面局部刷新
     * 对比页面缓存而言，如果并发量大，更有优势。主要利用静态化页面访问比动态页面快的原理
     * @param model
     * @param user
     * @param request
     * @param response
     * @param itemId
     * @return
     */
    @ApiOperation("静态化活动详情页面")
    @RequestMapping(value="/detail/{itemId}")  //produces="text/html"
    @ResponseBody
    public Result<ItemDetailVo> toDetail_staticPage(Model model,SeckillUser user,
                                                    HttpServletRequest request, HttpServletResponse response, @PathVariable("itemId")long itemId) {//id一般用snowflake算法
        ItemVo itemVo=itemService.getItemVoByItemId(itemId);
        model.addAttribute("user",user);
        model.addAttribute("itemVo",itemVo);
        //此外，秒杀页面还需要时间等参数
        int status=0;// 0 未开始 1进行中 2 结束了
        int restSecond=0;//倒计时时间
        Long start=itemVo.getStartDate().getTime();
        Long end=itemVo.getEndDate().getTime();
        Long now= System.currentTimeMillis();
        if(start<now&&now<end){
            //进行中
            status=1;
            restSecond=0;
        }else if (now>end){
            //超过活动时间
            status=2;
            restSecond=-1;
        }else{
            restSecond=(int)(start-now)/1000;
        }
        ItemDetailVo itemDetailVo=new ItemDetailVo();
        itemDetailVo.setItemVo(itemVo);
        itemDetailVo.setRemailSeconds(restSecond);
        itemDetailVo.setStatus(status);
        itemDetailVo.setUser(user);
        model.addAttribute("status",status);
        model.addAttribute("remailSeconds", restSecond);

        //返回的是这个页面所需要变动的所有数据，分装在ItemDetailVo
        return Result.success(itemDetailVo);
    }



}
