package com.leyou.auth.web;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.user.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/19 15:13
 * @Description:
 */
@Slf4j
@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private JwtProperties prop;

    /**
     * 功能描述:    实现登陆返回token
     *
     * @param:
     * @return:
     * @auther: 王忠强
     * @date: 2019/3/19 15:17
     */
    @PostMapping("login")
    public ResponseEntity<Void> login(@RequestParam("username")String username,
                                      @RequestParam("password")String password,
                                      HttpServletResponse response, HttpServletRequest request){
        //  登陆
        String token = authService.login(username,password);
        //  写入cookie
        log.info("username={},password={}",username,password);
        CookieUtils.newBuilder(response).httpOnly().request(request)
                .build(prop.getCookieName(),token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
    /**
     * 功能描述:    验证用户
     * @param:
     * @return: 
     * @auther: 王忠强
     * @date: 2019/3/21 11:25
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN")String token,
                                           HttpServletResponse response, HttpServletRequest request){
        if (StringUtils.isBlank(token)) {
            //没有token表示未登录返回403未授权
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        try {
            //解析token
            UserInfo info = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            //  刷新token，重新生成token
            String newToken = JwtUtils.generateToken(info, prop.getPrivateKey(), prop.getExpire());
            // 写入token
            CookieUtils.newBuilder(response).httpOnly().request(request)
                    .build(prop.getCookieName(),newToken);
            //登陆成功
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            //登陆失败或者登陆超时，token已过期或者token被篡改
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }
}
