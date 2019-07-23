package com.leyou.order.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/19 14:56
 * @Description:
 */
@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {

    private String secret;
    private String pubKeyPath;
    private String priKeyPath;
    private int expire;
    private String cookieName;
    private int cookieMaxAge;

    private PublicKey publicKey;

    //  对象一旦实例化后，就应该读取公钥和私钥
    @PostConstruct
    public  void init() throws Exception {
        //  读取公钥私钥
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }
}
