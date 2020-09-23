package ind.lcw.campus_seckill.service;

import ind.lcw.campus_seckill.entity.SeckillUser;
import ind.lcw.campus_seckill.result.CodeMsg;
import ind.lcw.campus_seckill.vo.LoginVo;

import javax.servlet.http.HttpServletResponse;

public interface SeckillUserService {
    public static final String COOKIE1_NAME_TOKEN="token";
    SeckillUser getSeckillUserById(Long id);
    SeckillUser getSeckillUserByToken(String token, HttpServletResponse response);
    public CodeMsg login(HttpServletResponse response, LoginVo loginVo);
    public void addCookie(SeckillUser user,String token,HttpServletResponse response);
}
