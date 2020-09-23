package ind.lcw.campus_seckill.service.ServiceImpl;

import ind.lcw.campus_seckill.mapper.SeckillUserMapper;
import ind.lcw.campus_seckill.util.MD5Util;
import ind.lcw.campus_seckill.util.UUIDUtil;
import ind.lcw.campus_seckill.vo.LoginVo;
import ind.lcw.campus_seckill.entity.SeckillUser;
import ind.lcw.campus_seckill.redis.RedisService;
import ind.lcw.campus_seckill.redis.SeckillUserKey;
import ind.lcw.campus_seckill.result.CodeMsg;
import ind.lcw.campus_seckill.service.SeckillUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
@Service
public class SeckillUserServiceIMpl implements SeckillUserService {

    @Autowired
    SeckillUserMapper seckillUserMapper;
    @Autowired
    RedisService redisService;

    /**
     * 缓存中有user就先拿，没有就去数据库拿，并设置缓存
     * @param id
     * @return
     */
    @Override
    public SeckillUser getSeckillUserById(Long id) {
        SeckillUser user=redisService.get(SeckillUserKey.getById,""+id,SeckillUser.class);//key必须是String
        if(user==null){
            user=seckillUserMapper.getById(id);
            redisService.set(SeckillUserKey.getById,""+id,user);
        }
        return user;
    }

    /**
     * 通过存储在redis缓存的token替代session，获取user
     * @param token
     * @param response
     * @return
     */
    @Override
    public SeckillUser getSeckillUserByToken(String token, HttpServletResponse response) {
        if(StringUtils.isEmpty(token))
            return null;
        //根据前缀和key获取data
        //前缀在redis包中，为每个service设置了对应的前置，避免key重复
        SeckillUser user=redisService.get(SeckillUserKey.token,token,SeckillUser.class);//通过string to bean方法将存储的value化为对应class对象的bean
        //访问就刷新使用时间(通过重新设置响应回来的cookie来实现)
        if(user!=null){
            addCookie(user,token,response);
        }
        return user;
    }


    /**
     *这里使用两次md5进行安全检验
     * 前端一次，后端一次
     * 成功登录会获取到token也就是sessionId，便于存取User对象
     * @param response
     * @param loginVo
     * @return
     */
    @Override
    public CodeMsg login(HttpServletResponse response, LoginVo loginVo) {
        if(loginVo==null)
            return CodeMsg.SERVER_ERROR;
        String oneMd5Pwd=loginVo.getPassword();
        String mobileNumber=loginVo.getMobile();//作为用户的Id
        SeckillUser user=seckillUserMapper.getById(Long.parseLong(mobileNumber));
        if(user==null){
            return CodeMsg.MOBILE_NOTEXIST;
        }
        String dbPwd=user.getPwd();
        String salt=user.getSalt();
        //对比
        if(!dbPwd.equals(MD5Util.formPassToDBPass(oneMd5Pwd,salt))){
            return CodeMsg.PASSWORD_ERROR;
        }
        //创建唯一的token,以及cookies，放入响应中
        String token= UUIDUtil.uuid();
        addCookie(user,token,response);
        return CodeMsg.SUCCESS;
    }

    /**
     * 此方法用于将token放到cookies中并响应回客户端
     * @param user
     * @param token
     * @param response
     */
    @Override
    public void addCookie(SeckillUser user, String token, HttpServletResponse response) {
        redisService.set(SeckillUserKey.token,token,user);//redis 就等同于session
        Cookie cookie=new Cookie(COOKIE1_NAME_TOKEN,token);// cookie只存token
        cookie.setMaxAge(SeckillUserKey.TOKEN_EXPIRE);//与token有效期保持一致
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
