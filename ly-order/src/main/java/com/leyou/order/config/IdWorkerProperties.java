package com.leyou.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/26 16:47
 * @Description:
 */
@Data
@ConfigurationProperties(prefix = "ly.worker")
public class IdWorkerProperties {

    private Long workerId;      //当前机器ID

    private Long dataCenterId;  //序列号
}
