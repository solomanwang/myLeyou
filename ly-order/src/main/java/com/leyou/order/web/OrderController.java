package com.leyou.order.web;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/26 15:07
 * @Description:
 */
@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDTO dto){
        return ResponseEntity.ok(orderService.createOrder(dto));
    }

    /**
     *
     * 功能描述: 根据id查询订单
     *
     * @param:
     * @return:
     * @auther: 王忠强
     * @date: 2019/3/27 15:25
     */
    @GetMapping("{id}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id")Long id){
        return ResponseEntity.ok(orderService.queryOrderById(id));
    }

    /**
     *
     * 功能描述: 创建支付链接
     *
     * @param:
     * @return:
     * @auther: 王忠强
     * @date: 2019/3/27 21:34
     */
    @GetMapping("/url/{id}")
    public ResponseEntity<String> createPayUrl(@PathVariable("id")Long id){
//        return ResponseEntity.ok(orderService.createPayUrl(id));
        return ResponseEntity.ok("www.baidu.com");
    }

    /**
     *
     * 功能描述: 查询订单状态
     *
     * @param:
     * @return:
     * @auther: 王忠强
     * @date: 2019/3/28 22:05
     */
    @GetMapping("/state/{id}")
    public ResponseEntity<Integer> queryOrderState(@PathVariable("id")Long id){
        return ResponseEntity.ok(orderService.queryOrderState(id).getValue());
    }
}
