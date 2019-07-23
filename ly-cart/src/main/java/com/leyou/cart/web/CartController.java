package com.leyou.cart.web;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/21 17:02
 * @Description:
 */
@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 功能描述:   添加购物擦
     *
     * @param:
     * @return:
     * @auther: 王忠强
     * @date: 2019/3/22 21:57
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
        cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     *
     * 功能描述:   查询购物车列表
     *
     * @param:
     * @return:
     * @auther: 王忠强
     * @date: 2019/3/22 21:57
     */
    @GetMapping("list")
    public ResponseEntity<List<Cart>> queryCartList(){
        return ResponseEntity.ok(cartService.queryCartList());
    }

    /**
     *
     * 功能描述:   修改购物车数量
     *
     * @param:
     * @return:
     * @auther: 王忠强
     * @date: 2019/3/25 14:55
     */
    @PutMapping
    public ResponseEntity<Void> updateCartNum(@RequestParam("id")Long id,@RequestParam("num")Integer num){
        cartService.updateCartNum(id,num);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     *
     * 功能描述:    根据id删除购物车中的商品
     *
     * @param:
     * @return:
     * @auther: 王忠强
     * @date: 2019/3/25 15:21
     */
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCartById(@PathVariable("skuId")String skuId){
        cartService.deleteCartById(skuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
