package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author: cuzz
 * @Date: 2018/10/31 19:20
 * @Description: 异常的枚举
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {
    PRICE_CANNOT_BE_NULL(400, "价格不能为空"),
    CATEGORY_NOT_FOUND(404, "商品分类没有找到"),
    SPEC_GROUP_NOT_FOUND(404, "商品规格组没有查到"),
    BRAND_SAVE_ERROR(500, "新增品牌失败"),
    GOODS_SAVE_ERROR(500, "新增商品失败"),
    BRAND_NOT_FOUND(404, "品牌没有找到"),
    UPLOAD_FILE_ERROR(500, "文件上传失败"),
    INVALID_FILE_TYPE(400, "无效文件类型"),
    INVALID_SIGN_ERROR(400, "无效的签名异常"),
    INVALID_ORDER_PARAM(400, "无订单金额不符"),
    SPEC_PARAM_NOT_FOUND(404,"商品规格参数不存在"),
    GOODS_NOT_FOUND(404,"商品不存在"),
    SPU_DETAIL_NOT_FOUND(404, "商品详情不存在"),
    GOODS_SKU_NOT_FOUND(404,"sku没有找到"),
    CATEGORY_NOT_FIND(404,"商品分类未查到"),
    SPEC_GROUP_NOT_FIND(404,"商品规格组未查到"),
    GOODS_NOT_FIND(404,"商品未查到"),
    GOODS_DETAIL_NOT_FIND(404,"商品详情未查到"),
    GOODS_SKU_NOT_FIND(404,"商品sku未查到"),
    GOODS_STOCK_NOT_FIND(404,"商品库存未查到"),
    GOODS_UPDATE_ERROR(500,"更新商品失败"),
    FILE_NOT_NULL(400,"文件不能为空"),
    GROUP_NOT_NULL(400,"参数不能为空"),
    ORDER_STATUS_ERROR(400,"订单状态无效"),
    GOODS_ID_CANNOT_BE_NULL(400,"商品id不能为空"),
    SAVE_ERROR(500,"新增失败"),
    CREATE_ORDER_ERROR(500,"新增订单失败"),
    DELE_ERROR(500,"删除失败"),
    UPDATE_ERROR(500,"修改失败"),
    CREATE_TOKEN_ERROR(500,"用户凭证生成失败"),
    STOCK_NOT_ENOUGH(500,"库存不足"),
    WX_PAY_ORDER_FAIL(500,"微信下单失败"),
    UPDTAE_ORDER_STATUS_ERROR(500,"更新订单状态失败"),
    INVALID_USER_DATA_TYPE(400,"用户数据类型无效"),
    INVALID_USERNAME_PASSWORD(400,"用户或者密码错误"),
    CREATE_USER_ERROR(500,"注册用户失败"),
    INVALID_VERIFY_CODE(400,"无效的验证码"),
    SPEC_GROUP_NOT_FOND(404,"商品规格组未查到"),
    SPEC_PARAM_NOT_FOND(404,"规格组参数未查到"),
    ORDER_NOT_FOND(404,"订单不存在"),
    ORDER_DETAIL_NOT_FOUND(404,"订单详情不存在"),
    ORDER_STATUS_NOT_FOUND(404,"订单状态不存在"),
    UNAUTHORIZED(403,"未授权登陆"),
    CART_NOT_FOUND(404,"购物车为空"),
    ;

    private int code;
    private String msg;
}
