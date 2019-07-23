package com.leyou.order.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/26 17:27
 * @Description:
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {
}
