package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.springboot.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 合并订单数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "合并后的订单信息")
public class MergedOrderDTO {
    
    @Schema(description = "订单ID")
    private Long id;
    
    @Schema(description = "订单编号")
    private String orderNo;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;
    
    @Schema(description = "支付方式")
    private String paymentMethod;
    
    @Schema(description = "支付时间")
    private LocalDateTime paymentTime;
    
    @Schema(description = "订单状态")
    private String status;
    
    @Schema(description = "收货地址")
    private String address;
    
    @Schema(description = "联系人")
    private String contactName;
    
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "订单备注")
    private String remark;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @Schema(description = "支付截止时间")
    private LocalDateTime paymentDeadline;
    
    @Schema(description = "订单商品列表")
    private List<Order> productList;
    
    /**
     * 根据订单列表创建合并订单
     * @param orders 订单列表
     * @return 合并后的订单
     */
    public static MergedOrderDTO fromOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return null;
        }
        
        Order firstOrder = orders.get(0);
        MergedOrderDTO mergedOrder = new MergedOrderDTO();
        mergedOrder.setId(firstOrder.getId());
        mergedOrder.setOrderNo(firstOrder.getOrderNo());
        mergedOrder.setUserId(firstOrder.getUserId());
        mergedOrder.setPaymentMethod(firstOrder.getPaymentMethod());
        mergedOrder.setPaymentTime(firstOrder.getPaymentTime());
        mergedOrder.setStatus(firstOrder.getStatus());
        mergedOrder.setAddress(firstOrder.getAddress());
        mergedOrder.setContactName(firstOrder.getContactName());
        mergedOrder.setContactPhone(firstOrder.getContactPhone());
        mergedOrder.setRemark(firstOrder.getRemark());
        mergedOrder.setCreateTime(firstOrder.getCreateTime());
        mergedOrder.setUpdateTime(firstOrder.getUpdateTime());
        mergedOrder.setPaymentDeadline(firstOrder.getPaymentDeadline());
        
        // 计算订单总金额
        BigDecimal totalAmount = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        mergedOrder.setTotalAmount(totalAmount);
        
        // 设置商品列表
        mergedOrder.setProductList(orders);
        
        return mergedOrder;
    }
}