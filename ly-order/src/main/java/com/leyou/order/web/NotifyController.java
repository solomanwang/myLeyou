package com.leyou.order.web;

import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/28 20:55
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("notify")
public class NotifyController {

    @Autowired
    private OrderService orderService;

    /**
     *
     * 功能描述: 微信支付成功回调
     *
     * @param:
     * @return:
     * @auther: 王忠强
     * @date: 2019/3/28 21:14
     */
    @PostMapping(value = "pay", produces = "application/xml")
    public Map<String,String> WXPAYnotify(@RequestBody Map<String,String> result){
        log.info("[支付回调] 接收微信支付回调，结果：{}",result);
        // 处理回调
        orderService.handleNotify(result);
        Map<String,String> msg = new HashMap<>();
        msg.put("return_code","SUCCESS");
        msg.put("return_msg","OK");
        return msg;
    }
}
