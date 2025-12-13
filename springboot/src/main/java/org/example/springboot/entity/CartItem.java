package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车项实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("cart_item")
@Schema(description = "购物车项信息")
public class CartItem {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "购物车项ID")
    private Long id;
    
    @Schema(description = "购物车ID")
    private Long cartId;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "商品ID")
    private Long productId;
    
    @Schema(description = "商品名称")
    private String productName;
    
    @Schema(description = "商品图片")
    private String productImage;
    
    @Schema(description = "商品单价")
    private BigDecimal price;
    
    @Schema(description = "商品数量")
    private Integer quantity;
    
    @Schema(description = "商品总金额")
    private BigDecimal totalAmount;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}