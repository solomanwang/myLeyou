package com.leyou.cart.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.cart.intercepyor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/21 17:04
 * @Description:
 */
@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "cart:uid:";

    //新增购物车
    public void addCart(Cart cart) {
        //  获取用户
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(key);
        String hashKey = cart.getSkuId().toString();
        Integer num = cart.getNum();
        //  判断当前购物擦商品是否存在
        if (ops.hasKey(hashKey)) {
            //  是，修改数量
            String jsonCart = ops.get(hashKey).toString();
            cart = JsonUtils.parse(jsonCart, Cart.class);
            cart.setNum(cart.getNum() + num);
        }
        cart.setUserId(user.getId());
        //  写回redis
        ops.put(hashKey,JsonUtils.serialize(cart));

    }
    //查询购物车列表
    public List<Cart> queryCartList() {
        //  获取用户
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();
        if (!redisTemplate.hasKey(key)){
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        //  获取用户所有购物车数据
        List<Cart> carts = redisTemplate.opsForHash().values(key).stream()
                .map(o -> JsonUtils.parse(o.toString(), Cart.class)).collect(Collectors.toList());
        return carts;
    }

    //  修改购物车数量
    public void updateCartNum(Long id, Integer num) {
        //  获取用户
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();
        String hashKey = id.toString();
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(key);
        Cart cart = JsonUtils.parse(ops.get(hashKey).toString(), Cart.class);
        cart.setNum(num);
        ops.put(hashKey,JsonUtils.serialize(cart));
    }

    //  根据id删除购物车内商品
    public void deleteCartById(String skuId) {
        //  获取用户
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();
        redisTemplate.opsForHash().delete(key, skuId);
    }
}
