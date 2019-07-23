package com.leyou.gateway.filters;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/21 14:14
 * @Description:
 */
@Component
@EnableConfigurationProperties({JwtProperties.class,FilterProperties.class})
public class AuthFilter extends ZuulFilter {

    @Autowired
    private JwtProperties prop;
    @Autowired
    private FilterProperties filterProp;

    //  选择过滤器类型
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;    //  前置过滤
    }

    //  过滤器顺序
    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1;
    }

    //  是否过滤
    @Override
    public boolean shouldFilter() {
        //  获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        //  获取request
        HttpServletRequest request = ctx.getRequest();
        //  获取请求路径
        String url = request.getRequestURI();
        //  判断是否放行，放行者返回false
        return !isAllowPath(url);
    }

    //  判断是否在白名单内
    private boolean isAllowPath(String url) {
        //  遍历白名单
        for (String path : filterProp.getAllowPaths()) {
            //  如果是配置文件中的路径就放行
            if (url.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    //  开始过滤
    @Override
    public Object run() throws ZuulException {
        //  获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        //  获取request
        HttpServletRequest request = ctx.getRequest();
        //  获取token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        //  解析token
        try {
            UserInfo user = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            // TODO 可以做权限校验

        } catch (Exception e) {
            //  解析token失败未登录，拦截
            ctx.setSendZuulResponse(false); //false表示拦截
            ctx.setResponseStatusCode(403); //返回状态码
        }
        return null;
    }
}
