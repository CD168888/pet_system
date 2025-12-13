package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.springboot.entity.CartItem;

import java.util.List;

/**
 * 购物车项Mapper接口
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
    
    /**
     * 根据购物车ID获取购物车项列表
     * @param cartId 购物车ID
     * @return 购物车项列表
     */
    @Select("SELECT * FROM cart_item WHERE cart_id = #{cartId}")
    List<CartItem> getCartItemsByCartId(Long cartId);
    
    /**
     * 根据用户ID获取购物车项列表
     * @param userId 用户ID
     * @return 购物车项列表
     */
    @Select("SELECT * FROM cart_item WHERE user_id = #{userId}")
    List<CartItem> getCartItemsByUserId(Long userId);
    
    /**
     * 根据购物车ID和商品ID获取购物车项
     * @param cartId 购物车ID
     * @param productId 商品ID
     * @return 购物车项
     */
    @Select("SELECT * FROM cart_item WHERE cart_id = #{cartId} AND product_id = #{productId}")
    CartItem getCartItemByCartIdAndProductId(Long cartId, Long productId);
    
    /**
     * 根据购物车ID删除所有购物车项
     * @param cartId 购物车ID
     * @return 删除的数量
     */
    @Delete("DELETE FROM cart_item WHERE cart_id = #{cartId}")
    int deleteCartItemsByCartId(Long cartId);
    
    /**
     * 根据用户ID和商品ID获取购物车项
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 购物车项
     */
    @Select("SELECT * FROM cart_item WHERE user_id = #{userId} AND product_id = #{productId}")
    CartItem getCartItemByUserIdAndProductId(Long userId, Long productId);
}
