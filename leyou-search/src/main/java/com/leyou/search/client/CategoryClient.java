package com.leyou.search.client;

import com.leyou.item.api.CategoryApi;
import com.leyou.item.pojo.Category;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/4 17:15
 * @Description:
 */
//对应微服务的名称
@FeignClient("item-service")
public interface CategoryClient extends CategoryApi{
}
