package com.leyou.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/26 15:14
 * @Description:
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CartDTO {
    private Long skuId; //商品skuId
    private Integer num;//购买数量
}
