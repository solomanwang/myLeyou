package com.leyou.order.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.interceptor.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/26 15:08
 * @Description:
 */
@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper detailMapper;
    @Autowired
    private OrderStatusMapper statusMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private PayHelper payHelper;

    //  创建订单
    @Transactional
    public Long createOrder(OrderDTO dto) {

        //  1,新增订单
        Order order = new Order();
        //  1.1 订单编号，基本信息
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(dto.getPaymentType());
        //  1.2 用户信息
        UserInfo user = UserInterceptor.getUser();
        order.setBuyerNick(user.getUsername());
        order.setUserId(user.getId());
        order.setBuyerRate(false);
        //  1.3 收获人信息
        AddressDTO addr = AddressClient.findById(dto.getAddressId());
        order.setReceiver(addr.getName());
        order.setReceiverAddress(addr.getAddress());
        order.setReceiverCity(addr.getCity());
        order.setReceiverDistrict(addr.getDistrict());
        order.setReceiverMobile(addr.getPhone());
        order.setReceiverState(addr.getState());
        order.setReceiverZip(addr.getZipCode());
        //  1.4 金额
        Map<Long, Integer> numMap = dto.getCarts().stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        Set<Long> ids = numMap.keySet();
        List<Sku> skus = goodsClient.querySkuByIds(new ArrayList<>(ids));
        //  准备orderDetali集合
        ArrayList<OrderDetail> detalis = new ArrayList<>();
        //  计算总金额
        Long totalPay = 0L;
        for (Sku sku : skus) {
            totalPay += sku.getPrice() * numMap.get(sku.getId());   //计算总价
            //  准备orderDetail
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(),","));
            orderDetail.setNum(numMap.get(sku.getId()));
            orderDetail.setOrderId(orderId);
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setPrice(sku.getPrice());
            orderDetail.setSkuId(sku.getId());
            orderDetail.setTitle(sku.getTitle());
            detalis.add(orderDetail);
        }
        order.setTotalPay(totalPay);
        //  实付金额 = 总金额 + 邮费 - 优惠金额
        order.setActualPay(totalPay + order.getPostFee() - 0);

        //  1.5 order写入数据库
        int count = orderMapper.insertSelective(order);
        if (count != 1){
            log.error("[订单微服务] 新增订单失败，orderId{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        //  2 新增订单详情
        count = detailMapper.insertList(detalis);
        if (count != detalis.size()){
            log.error("[订单微服务] 新增订单详情失败，orderId{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        //  3 新增订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setStatus(OrderStatusEnum.UNPAY.getCode());
        count = statusMapper.insertSelective(orderStatus);
        if (count != 1){
            log.error("[订单微服务] 新增订单状态失败，orderId{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        //  4 调用商品微服务减库存
        goodsClient.decreaseStock(dto.getCarts());
        return orderId;
    }

    //根据id查询order
    public Order queryOrderById(Long id) {
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOND);
        }
        //查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> details = detailMapper.select(detail);
        if (CollectionUtils.isEmpty(details)) {
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        order.setOrderDetails(details);
        //查询订单状态
        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(id);
        if (orderStatus == null) {
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }
        order.setOrderStatus(orderStatus);
        return order;
    }
    //创建付款链接
    public String createPayUrl(Long id) {
        //查询订单
        Order order = this.queryOrderById(id);
        //健壮性判断
        if (order.getOrderStatus().getStatus() != OrderStatusEnum.UNPAY.getCode()){
            //订单状态无效
            throw new LyException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        //支付金额
        Long actualPay = order.getActualPay();
        //商品描述
        String title = order.getOrderDetails().get(0).getTitle();
        return payHelper.createOrder(id,actualPay,title);
    }

    //处理微信支付回调
    public void handleNotify(Map<String,String> result) {
        //  数据校验
        payHelper.isSuccess(result);
        //  校验签名
        payHelper.isValidSign(result);
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
        //  全部检验通过修改订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setStatus(OrderStatusEnum.PAYED.getCode());
        orderStatus.setOrderId(order.getOrderId());
        order.setCreateTime(new Date());
        int count = statusMapper.updateByPrimaryKeySelective(orderStatus);
        if (count != 1){
            throw new LyException(ExceptionEnum.UPDTAE_ORDER_STATUS_ERROR);
        }
        log.info("[支付回调] 订单支付成功，订单号:{}",order.getOrderId());
    }

    //查询订单状态
    public PayState queryOrderState(Long id) {
        OrderStatus order = statusMapper.selectByPrimaryKey(id);
        Integer status = order.getStatus();
        if (status != OrderStatusEnum.UNPAY.getCode()){
            //已支付返回支付成功
            return PayState.SUCCESS;
        }
        //  如果是未支付，但其实不一定是未支付，必须去微信查询支付状态
        return payHelper.queryPayState(id);
    }
}
