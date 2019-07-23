package com.leyou.sms.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/14 17:06
 * @Description:
 */
@Slf4j
@Component
//@EnableConfigurationProperties(SmsProperties.class)
public class Smsutils {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "sms:phone:";
    private static final Long SMS_MIN_INTERVAL_IN_MILLIS = 60000L;

    public void sendSms(String phone,
//            ,String signName,String templateCode,
                String templateParam
    ){
        //限流
        String key = KEY_PREFIX + phone;
        String lastTime = redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(lastTime)){
            Long last = Long.valueOf(lastTime);
            if (System.currentTimeMillis() - last < SMS_MIN_INTERVAL_IN_MILLIS){
                log.info("[短信服务] 发送短信频率过高，被拦截，拦截手机号码{}",phone);
                return ;
            }
        }
        log.info("[短信服务] 向手机号码{}发送验证码",phone);
        redisTemplate.opsForValue().set(key,String.valueOf(System.currentTimeMillis()),1,TimeUnit.MINUTES);
    }
}
