package com.leyou.order.config;

import com.github.wxpay.sdk.WXPayConfig;
import lombok.Data;

import java.io.InputStream;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/27 17:08
 * @Description:
 */
@Data
public class PayConfig implements WXPayConfig {

    private String appID;   //公众号Id
    private String mchID;   //商户号
    private String key;   //生成签名的密钥
    private int httpConnectTimeoutMs;   //连接超时时间
    private int httpReadTimeoutMs;   //读取超时时间
    private String notifyUrl;   //下单通知回调地址

    @Override
    public String getAppID() {
        return null;
    }

    @Override
    public String getMchID() {
        return null;
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public InputStream getCertStream() {
        return null;
    }

    @Override
    public int getHttpConnectTimeoutMs() {
        return 0;
    }

    @Override
    public int getHttpReadTimeoutMs() {
        return 0;
    }
}
