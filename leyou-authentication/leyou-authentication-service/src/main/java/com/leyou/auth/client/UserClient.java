package com.leyou.auth.client;

import com.leyou.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/19 16:03
 * @Description:
 */
@FeignClient("user-service")
public interface UserClient extends UserApi {
}
