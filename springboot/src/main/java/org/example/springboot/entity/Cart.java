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
 * 购物车实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("cart")
@Schema(description = "购物车信息")
public class Cart {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "购物车ID")
    private Long id;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "商品总数量")
    private Integer totalQuantity;
    
    @Schema(description = "商品总金额")
    private BigDecimal totalAmount;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}