package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.example.springboot.DTO.MergedOrderDTO;
import org.example.springboot.DTO.OrderCreateDTO;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Order;
import org.example.springboot.entity.User;
import org.example.springboot.service.AlipayService;
import org.example.springboot.service.OrderService;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 */
@Tag(name = "订单管理接口")
@RestController
@RequestMapping("/order")
public class OrderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    @Resource
    private OrderService orderService;

    @Resource
    private AlipayService alipayService;

    @Operation(summary = "创建订单")
    @PostMapping
    public Result<?> createOrder(@RequestBody OrderCreateDTO orderCreateDTO, HttpSession session) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        // 默认使用支付宝支付
        orderCreateDTO.setPaymentMethod("支付宝");
        List<Order> orders = orderService.createOrder(userId, orderCreateDTO, session);
        return Result.success("订单创建成功", orders);
    }

    @Operation(summary = "支付订单")
    @PostMapping("/pay/{orderId}")
    public Result<?> payOrder(@PathVariable Long orderId) {
        try {
            // 获取订单信息
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }

            Map<String, String> result = alipayService.createQrCodePay(order);

            return Result.success("生成支付二维码成功", result);
        } catch (Exception e) {
            LOGGER.error("支付失败", e);
            return Result.error("支付失败：" + e.getMessage());
        }
    }

    @Operation(summary = "检查支付状态")
    @GetMapping("/check/{orderId}")
    public Result<?> checkPaymentStatus(@PathVariable Long orderId) {
        try {
            // 获取订单信息
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }

            // 查询支付状态
            String status = alipayService.queryPaymentStatus(order.getOrderNo());

            return Result.success(status);
        } catch (Exception e) {
            LOGGER.error("查询支付状态失败", e);
            return Result.error("查询支付状态失败：" + e.getMessage());
        }
    }

    @Operation(summary = "根据ID获取订单")
    @GetMapping("/{id}")
    public Result<?> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return Result.success(order);
    }

    @Operation(summary = "获取用户订单列表")
    @GetMapping("/user")
    public Result<?> getUserOrders(
            @RequestParam(required = false) String orderNo) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        List<Order> orders = orderService.getOrdersByUserIdAndOrderNo(userId, orderNo);
        return Result.success(orders);
    }

    @Operation(summary = "分页查询订单")
    @GetMapping("/page")
    public Result<?> getOrdersByPage(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        // 获取当前用户信息，判断是否为管理员
        User currentUser = JwtTokenUtils.getCurrentUser();
        // 如果是管理员，userId设为null，查询所有订单
        if (currentUser != null && "ADMIN".equals(currentUser.getRoleCode())) {
            userId = null;
        }
        Page<Order> page = orderService.getOrdersByPage(userId, status, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "分页查询合并后的订单")
    @GetMapping("/merged/page")
    public Result<?> getMergedOrdersByPage(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        // 获取当前用户信息，判断是否为管理员
        User currentUser = JwtTokenUtils.getCurrentUser();
        // 如果是管理员，userId设为null，查询所有合并订单
        if (currentUser != null && "ADMIN".equals(currentUser.getRoleCode())) {
            userId = null;
        }
        Page<MergedOrderDTO> page = orderService.getMergedOrdersByPage(userId, status, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "更新订单状态")
    @PutMapping("/{orderNo}/status")
    public Result<?> updateOrderStatus(@PathVariable String orderNo, @RequestParam String status) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        boolean success = orderService.updateOrderStatusByOrderNo(orderNo, userId, status);
        if (success) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result<?> cancelOrder(@PathVariable Long id) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        boolean success = orderService.cancelOrder(id, userId);
        if (success) {
            return Result.success("取消成功");
        } else {
            return Result.error("取消失败");
        }
    }

    @Operation(summary = "确认收货")
    @PutMapping("/{id}/confirm")
    public Result<?> confirmReceipt(@PathVariable Long id) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        boolean success = orderService.confirmReceipt(id, userId);
        if (success) {
            return Result.success("确认收货成功");
        } else {
            return Result.error("确认收货失败");
        }
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/{id}")
    public Result<?> deleteOrder(@PathVariable Long id) {
        boolean success = orderService.deleteOrder(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    @Operation(summary = "获取订单支付剩余时间")
    @GetMapping("/{id}/payment-time")
    public Result<?> getOrderPaymentRemainingTime(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }

        long remainingSeconds = orderService.getRemainingPaymentTime(order);
        boolean isExpired = orderService.isOrderExpired(order);

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", id);
        data.put("remainingSeconds", remainingSeconds);
        data.put("isExpired", isExpired);
        data.put("paymentDeadline", order.getPaymentDeadline());

        return Result.success(data);
    }
} 