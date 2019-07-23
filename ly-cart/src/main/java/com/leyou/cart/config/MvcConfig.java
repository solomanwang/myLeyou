package com.leyou.cart.config;

import com.leyou.cart.intercepyor.UserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/21 16:50
 * @Description:
 */
@EnableConfigurationProperties(JwtProperties.class)
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    //
    @Autowired
    private JwtProperties prop;
    //  添加拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInterceptor(prop)).addPathPatterns("/**");
    }
}
