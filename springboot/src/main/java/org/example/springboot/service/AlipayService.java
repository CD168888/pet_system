package org.example.springboot.service;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.easysdk.factory.Factory;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.config.AlipayConfig;
import org.example.springboot.entity.Order;
import org.example.springboot.enumClass.OrderStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付服务类
 */
@Service
public class AlipayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlipayService.class);

    @Resource
    private AlipayConfig aliPayConfig;

    @Resource
    private OrderService orderService;

    private static final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String FORMAT = "JSON";
    private static final String CHARSET = "utf-8";
    private static final String SIGN_TYPE = "RSA2";


    /**
     * 生成二维码支付链接
     *
     * @param order 订单信息
     * @return 二维码链接和订单号
     * @throws Exception 支付异常
     */
    public Map<String, String> createQrCodePay(Order order) throws Exception {

        try {
            AlipayClient alipayClient = new DefaultAlipayClient(
                    GATEWAY_URL, aliPayConfig.getAppId(),
                    aliPayConfig.getAppPrivateKey(),
                    FORMAT, CHARSET,
                    aliPayConfig.getAlipayPublicKey(),
                    SIGN_TYPE
            );

            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();

            request.setNotifyUrl(aliPayConfig.getNotifyUrl());

            request.setBizContent("{\"out_trade_no\":\"" + order.getOrderNo() + "\"," +
                    "\"total_amount\":\"" + order.getTotalAmount().setScale(2, RoundingMode.HALF_UP) + "\"," +
                    "\"subject\":\"" + order.getProductName() + "\"}");

            AlipayTradePrecreateResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                LOGGER.info("支付宝API调用成功，二维码链接：{}", response.getQrCode());

                Map<String, String> result = new HashMap<>();
                result.put("qrCode", response.getQrCode());
                result.put("orderNum", order.getOrderNo());

                return result;
            } else {
                String errorMsg = response.getSubMsg();
                LOGGER.error("支付宝API调用失败，错误信息：{}", errorMsg);
                
                // 处理特殊情况：交易已经支付
                if (errorMsg != null && errorMsg.contains("交易已经支付")) {
                    LOGGER.info("交易已经支付，更新订单状态，订单号：{}", order.getOrderNo());
                    
                    // 检查并更新订单状态
                    if ("待付款".equals(order.getStatus())) {
                        orderService.updateOrderStatusByOrderNo(order.getOrderNo(), order.getUserId(), "待发货");
                        LOGGER.info("订单状态已更新为待发货：{}", order.getOrderNo());
                    }
                    
                    // 返回一个模拟的成功响应，包含订单号
                    Map<String, String> result = new HashMap<>();
                    result.put("qrCode", ""); // 空二维码，因为交易已支付
                    result.put("orderNum", order.getOrderNo());
                    return result;
                } else {
                    throw new Exception("下单失败：" + errorMsg);
                }
            }
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            LOGGER.error("生成二维码失败，订单号：{}，错误信息：{}", order.getOrderNo(), errorMsg, e);
            
            // 处理特殊情况：交易已经支付
            if (errorMsg != null && errorMsg.contains("交易已经支付")) {
                LOGGER.info("交易已经支付，更新订单状态，订单号：{}", order.getOrderNo());
                
                // 检查并更新订单状态
                if ("待付款".equals(order.getStatus())) {
                    orderService.updateOrderStatusByOrderNo(order.getOrderNo(), order.getUserId(), "待发货");
                    LOGGER.info("订单状态已更新为待发货：{}", order.getOrderNo());
                }
                
                // 返回一个模拟的成功响应，包含订单号
                Map<String, String> result = new HashMap<>();
                result.put("qrCode", ""); // 空二维码，因为交易已支付
                result.put("orderNum", order.getOrderNo());
                return result;
            } else {
                throw e;
            }
        }
    }

    /**
     * 查询支付状态
     * @param orderNo 订单号
     * @return 支付状态
     * @throws Exception 查询异常
     */
    public String queryPaymentStatus(String orderNo) throws Exception {
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(
                    GATEWAY_URL,
                    aliPayConfig.getAppId(),
                    aliPayConfig.getAppPrivateKey(),
                    FORMAT,
                    CHARSET,
                    aliPayConfig.getAlipayPublicKey(),
                    SIGN_TYPE
            );
            // 构造查询请求
            // 创建支付宝交易查询请求对象
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            // 设置业务参数，指定要查询的商户订单号
            request.setBizContent("{\"out_trade_no\":\"" + orderNo + "\"}");
            // 执行查询请求，获取支付宝响应结果
            AlipayTradeQueryResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                if ("TRADE_SUCCESS".equals(tradeStatus)) {
                    // 支付成功，更新订单状态
                    Order order = orderService.getOrderByOrderNo(orderNo);
                    if (order != null && "待付款".equals(order.getStatus())) {
                        orderService.updateOrderStatusByOrderNo(orderNo, order.getUserId(), "待发货");
                        LOGGER.info("订单支付成功并更新状态：{}", orderNo);
                    }
                    return "已支付";
                } else if ("WAIT_BUYER_PAY".equals(tradeStatus)) {
                    return "未支付";
                } else {
                    return "支付失败";
                }
            } else {
                String subCode = response.getSubCode();
                // 处理交易不存在的情况，视为未支付
                if (subCode != null && "ACQ.TRADE_NOT_EXIST".equals(subCode)) {
                    LOGGER.info("交易不存在，视为未支付：{}", orderNo);
                    return "未支付";
                }
                return "查询失败";
            }
        } catch (Exception e) {
            // 检查是否是交易不存在的异常
            if (e.getMessage() != null && e.getMessage().contains("ACQ.TRADE_NOT_EXIST")) {
                LOGGER.info("交易不存在，视为未支付：{}", orderNo);
                return "未支付";
            }
            LOGGER.error("查询支付状态失败，订单号：{}", orderNo, e);
            return "查询失败";
        }
    }
}