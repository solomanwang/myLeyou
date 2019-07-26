package com.leyou.order.web;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
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
@Api("订单微服务接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    @ApiOperation(value = "创建订单接口，返回订单编号",notes = "传入订单dto对象在后台生成order对象然后返回编号")
    @ApiImplicitParam(name = "orderDTO",required = true,value = "订单DTO json对象，包含收货地址id，订单条目和支付类型")
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
    @ApiOperation(value = "根据id查询订单",notes = "根据订单id查询某个订单,返回订单对象")
    @ApiImplicitParam(name = "orderId",required = true,value = "long 类型id")
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
    @ApiOperation(value = "根据id创建支付链接",notes = "根据订单id创建微信支付的支付链接，返回支付链接的url")
    @ApiImplicitParam(name = "orderId",required = true,value = "long 类型id")
    public ResponseEntity<String> createPayUrl(@PathVariable("id")Long id){
        return ResponseEntity.ok(orderService.createPayUrl(id));
//        return ResponseEntity.ok("www.baidu.com");
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
    @ApiOperation(value = "根据id查询订单状态",notes = "根据订单id查询某个订单的支付状态，返回状态码")
    @ApiImplicitParam(name = "orderId",required = true,value = "long 类型id")
    public ResponseEntity<Integer> queryOrderState(@PathVariable("id")Long id){
        return ResponseEntity.ok(orderService.queryOrderState(id).getValue());
    }
}
