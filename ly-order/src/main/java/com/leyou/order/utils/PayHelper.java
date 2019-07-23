package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConfig;
import static com.github.wxpay.sdk.WXPayConstants.*;    //静态导入

import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.order.config.PayConfig;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/27 17:26
 * @Description:
 */
@Slf4j
@Component
public class PayHelper {
    @Autowired
    private WXPay wxPay;
    @Autowired
    private PayConfig config;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderStatusMapper statusMapper;

    public String createOrder(Long orderId,Long totalPay,String desc){
        try {
            Map<String,String> data = new HashMap<>();
            //商品描述
            data.put("body",desc);
            //订单号
            data.put("out_trade_no",orderId.toString());
            //金额，单位是分
            data.put("total_fee",totalPay.toString());
            //调用微信支付终端的ip
            data.put("spbill_create_ip","127.0.0.1");
            //回调地址
            data.put("notify_url",config.getNotifyUrl());
            //交易类型为扫码支付
            data.put("trade_type","NATIVE");
            //利用wxPay工具完成下单
            Map<String,String> result = wxPay.unifiedOrder(data);
            //判断通信和业务标识
            isSuccess(result);

            String url = result.get("code_url");
            return url;
        }catch (Exception e){
            log.error("[微信下单] 创建预交易订单异常失败:{}",e);
            return null;
        }
    }
    //判断通信和业务标识
    public void isSuccess(Map<String, String> result) {
        //判断通信成功或者失败
        String returnCode = result.get("return_code");
        if (StringUtils.equals(returnCode,FAIL)){
            //通信失败
            log.error("[微信下单] 微信下单通信失败，失败原因：{}",result.get("return_msg"));
            throw new LyException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }
        String resultCode = result.get("result_code");
        if (StringUtils.equals(resultCode,FAIL)){
            //通信失败
            log.error("[微信下单] 微信下单业务失败，错误码:{}失败原因:{}",result.get("err_code"),result.get("err_code_des"));
            throw new LyException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }
    }
    //  校验签名
    public void isValidSign(Map<String, String> sign) {
       try { // 重新生成签名，因为不确定微信回调签名生成类型，所以生成两个签名
           String sign3 = sign.get("sign");
           String sign1 = WXPayUtil.generateSignature(sign, config.getKey(), SignType.HMACSHA256);
           String sign2 = WXPayUtil.generateSignature(sign, config.getKey(), SignType.MD5);
           //比对签名
           if (!StringUtils.equals(sign3,sign1) && !StringUtils.equals(sign3,sign2)){
               //签名有误，抛出异常
               throw new LyException(ExceptionEnum.INVALID_SIGN_ERROR);
           }
       }catch (Exception e){
           throw new LyException(ExceptionEnum.INVALID_SIGN_ERROR);
       }
    }

    // 查询微信支付是否成功
    public PayState queryPayState(Long id) {
        try{
            //封装请求参数
            Map<String, String> data = new HashMap<>();
            data.put("out_trade_no",id.toString());
            //  查询返回结果
            Map<String, String> result = wxPay.orderQuery(data);
            //  数据校验
            this.isSuccess(result);
            //  校验签名
            this.isValidSign(result);
            //  校验付款金额是否一致
            //  获取结果金额
            String totalFeeStr = result.get("total_fee");
            Long totalFee = Long.valueOf(totalFeeStr);
            //  获取订单ID
            String outTradeNoStr = result.get("out_trade_no");
            Long outTradeNo = Long.valueOf(outTradeNoStr);
            Order order = orderMapper.selectByPrimaryKey(outTradeNo);
            if (order.getActualPay() != totalFee){
                //  订单金额不符合
                throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
            }

            String state = result.get("trade_state");
            if (SUCCESS.equals(state)){
                //支付成功
                //  修改订单状态
                OrderStatus orderStatus = new OrderStatus();
                orderStatus.setStatus(OrderStatusEnum.PAYED.getCode());
                orderStatus.setOrderId(order.getOrderId());
                order.setCreateTime(new Date());
                int count = statusMapper.updateByPrimaryKeySelective(orderStatus);
                if (count != 1){
                    throw new LyException(ExceptionEnum.UPDTAE_ORDER_STATUS_ERROR);
                }
                return PayState.SUCCESS;
            }
            if ("NOTPAY".equals(state) || "USERPAYING".equals(state)){
                return PayState.NOT_PAY;
            }
            return PayState.FAIL;
        }catch (Exception e){
                return PayState.NOT_PAY;
        }
    }
}
