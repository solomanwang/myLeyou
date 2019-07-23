package com.leyou.search.client;

import com.leyou.item.api.SpecificationApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/5 09:36
 * @Description:
 */
@FeignClient("item-service")
public interface SpecificationClient extends SpecificationApi {
}
