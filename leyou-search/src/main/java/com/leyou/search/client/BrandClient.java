package com.leyou.search.client;

import com.leyou.item.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/5 09:35
 * @Description:
 */
@FeignClient("item-service")
public interface BrandClient extends BrandApi {
}
