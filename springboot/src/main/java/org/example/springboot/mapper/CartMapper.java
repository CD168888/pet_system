package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.springboot.entity.Cart;

/**
 * 购物车Mapper接口
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {
    
    /**
     * 根据用户ID获取购物车
     * @param userId 用户ID
     * @return 购物车对象
     */
    @Select("SELECT * FROM cart WHERE user_id = #{userId}")
    Cart getCartByUserId(Long userId);
}
