package org.example.springboot.service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.example.springboot.DTO.CartDTO;
import org.example.springboot.DTO.CartItemDTO;
import org.example.springboot.entity.Cart;
import org.example.springboot.entity.CartItem;
import org.example.springboot.entity.Product;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.CartItemMapper;
import org.example.springboot.mapper.CartMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类
 */
@Service
public class CartService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CartService.class);
    
    @Resource
    private ProductService productService;
    
    @Resource
    private CartMapper cartMapper;
    
    @Resource
    private CartItemMapper cartItemMapper;
    
    /**
     * 添加商品到购物车
     * @param session HTTP会话
     * @param productId 商品ID
     * @param quantity 数量
     * @return 购物车DTO
     */
    @Transactional
    public CartDTO addToCart(HttpSession session, Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new ServiceException("商品数量必须大于0");
        }
        
        // 获取当前用户ID
        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            throw new ServiceException("用户未登录");
        }
        
        // 获取商品信息
        Product product = productService.getProductById(productId);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        
        // 检查商品是否上架
        if (product.getStatus() != 1) {
            throw new ServiceException("商品已下架");
        }
        
        // 检查库存是否充足
        if (product.getStock() < quantity) {
            throw new ServiceException("商品库存不足");
        }
        
        // 获取或创建用户购物车
        Cart cart = getOrCreateCart(userId);
        
        // 检查购物车中是否已存在该商品
        CartItem cartItem = cartItemMapper.getCartItemByCartIdAndProductId(cart.getId(), productId);
        
        if (cartItem != null) {
            // 更新商品数量
            int newQuantity = cartItem.getQuantity() + quantity;
            
            // 再次检查库存是否充足
            if (product.getStock() < newQuantity) {
                throw new ServiceException("商品库存不足");
            }
            
            cartItem.setQuantity(newQuantity);
            cartItem.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(newQuantity)).setScale(2, RoundingMode.HALF_UP));
            cartItem.setUpdateTime(LocalDateTime.now());
            
            cartItemMapper.updateById(cartItem);
        } else {
            // 创建新的购物车项
            CartItem newItem = new CartItem();
            newItem.setCartId(cart.getId());
            newItem.setUserId(userId);
            newItem.setProductId(product.getId());
            newItem.setProductName(product.getName());
            if(product.getImages()!=null){
                newItem.setProductImage(product.getImages().split(",")[0]); // 取第一张图片
            }
            newItem.setPrice(product.getPrice());
            newItem.setQuantity(quantity);
            newItem.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP));
            newItem.setCreateTime(LocalDateTime.now());
            newItem.setUpdateTime(LocalDateTime.now());
            
            cartItemMapper.insert(newItem);
        }
        
        // 更新购物车总数量和总金额
        updateCartTotal(cart.getId());
        
        // 返回购物车DTO
        return getCart(session);
    }
    
    /**
     * 更新购物车商品数量
     * @param session HTTP会话
     * @param productId 商品ID
     * @param quantity 新数量
     * @return 购物车DTO
     */
    @Transactional
    public CartDTO updateCartItemQuantity(HttpSession session, Long productId, Integer quantity) {
        if (quantity <= 0) {
            // 如果数量小于等于0，则移除该商品
            return removeFromCart(session, productId);
        }
        
        // 获取当前用户ID
        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            throw new ServiceException("用户未登录");
        }
        
        // 获取商品信息
        Product product = productService.getProductById(productId);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        
        // 检查库存是否充足
        if (product.getStock() < quantity) {
            throw new ServiceException("商品库存不足");
        }
        
        // 获取用户购物车
        Cart cart = cartMapper.getCartByUserId(userId);
        if (cart == null) {
            throw new ServiceException("购物车不存在");
        }
        
        // 获取购物车项
        CartItem cartItem = cartItemMapper.getCartItemByCartIdAndProductId(cart.getId(), productId);
        if (cartItem == null) {
            throw new ServiceException("购物车中不存在该商品");
        }
        
        // 更新商品数量
        cartItem.setQuantity(quantity);
        cartItem.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP));
        cartItem.setUpdateTime(LocalDateTime.now());
        
        cartItemMapper.updateById(cartItem);
        
        // 更新购物车总数量和总金额
        updateCartTotal(cart.getId());
        
        // 返回购物车DTO
        return getCart(session);
    }
    
    /**
     * 从购物车移除商品
     * @param session HTTP会话
     * @param productId 商品ID
     * @return 购物车DTO
     */
    @Transactional
    public CartDTO removeFromCart(HttpSession session, Long productId) {
        // 获取当前用户ID
        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            throw new ServiceException("用户未登录");
        }
        
        // 获取用户购物车
        Cart cart = cartMapper.getCartByUserId(userId);
        if (cart == null) {
            throw new ServiceException("购物车不存在");
        }
        
        // 获取购物车项
        CartItem cartItem = cartItemMapper.getCartItemByCartIdAndProductId(cart.getId(), productId);
        if (cartItem != null) {
            // 移除购物车项
            cartItemMapper.deleteById(cartItem.getId());
            
            // 更新购物车总数量和总金额
            updateCartTotal(cart.getId());
        }
        
        // 返回购物车DTO
        return getCart(session);
    }
    
    /**
     * 清空购物车
     * @param session HTTP会话
     * @return 空购物车DTO
     */
    @Transactional
    public CartDTO clearCart(HttpSession session) {
        // 获取当前用户ID
        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            throw new ServiceException("用户未登录");
        }
        
        // 获取用户购物车
        Cart cart = cartMapper.getCartByUserId(userId);
        if (cart != null) {
            // 删除所有购物车项
            cartItemMapper.deleteCartItemsByCartId(cart.getId());
            
            // 更新购物车总数量和总金额
            cart.setTotalQuantity(0);
            cart.setTotalAmount(BigDecimal.ZERO);
            cart.setUpdateTime(LocalDateTime.now());
            
            cartMapper.updateById(cart);
        }
        
        // 返回空购物车
        CartDTO cartDTO = new CartDTO();
        cartDTO.setItems(new ArrayList<>());
        cartDTO.setTotalQuantity(0);
        cartDTO.setTotalAmount(BigDecimal.ZERO);
        return cartDTO;
    }
    
    /**
     * 获取购物车
     * @param session HTTP会话
     * @return 购物车DTO
     */
    public CartDTO getCart(HttpSession session) {
        // 获取当前用户ID
        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            // 用户未登录，返回空购物车
            CartDTO cartDTO = new CartDTO();
            cartDTO.setItems(new ArrayList<>());
            cartDTO.setTotalQuantity(0);
            cartDTO.setTotalAmount(BigDecimal.ZERO);
            return cartDTO;
        }
        
        // 获取用户购物车
        Cart cart = cartMapper.getCartByUserId(userId);
        if (cart == null) {
            // 购物车不存在，返回空购物车
            CartDTO cartDTO = new CartDTO();
            cartDTO.setItems(new ArrayList<>());
            cartDTO.setTotalQuantity(0);
            cartDTO.setTotalAmount(BigDecimal.ZERO);
            return cartDTO;
        }
        
        // 获取购物车项列表
        List<CartItem> cartItems = cartItemMapper.getCartItemsByCartId(cart.getId());
        
        // 转换为CartItemDTO列表
        List<CartItemDTO> itemList = cartItems.stream().map(item -> {
            CartItemDTO dto = new CartItemDTO();
            dto.setProductId(item.getProductId());
            dto.setProductName(item.getProductName());
            dto.setProductImage(item.getProductImage());
            dto.setPrice(item.getPrice());
            dto.setQuantity(item.getQuantity());
            dto.setTotalAmount(item.getTotalAmount());
            return dto;
        }).collect(Collectors.toList());
        
        // 创建购物车DTO
        CartDTO cartDTO = new CartDTO();
        cartDTO.setItems(itemList);
        cartDTO.setTotalQuantity(cart.getTotalQuantity());
        cartDTO.setTotalAmount(cart.getTotalAmount());
        
        return cartDTO;
    }
    
    /**
     * 获取或创建用户购物车
     * @param userId 用户ID
     * @return 购物车对象
     */
    private Cart getOrCreateCart(Long userId) {
        Cart cart = cartMapper.getCartByUserId(userId);
        
        if (cart == null) {
            // 创建新购物车
            cart = new Cart();
            cart.setUserId(userId);
            cart.setTotalQuantity(0);
            cart.setTotalAmount(BigDecimal.ZERO);
            cart.setCreateTime(LocalDateTime.now());
            cart.setUpdateTime(LocalDateTime.now());
            
            cartMapper.insert(cart);
        }
        
        return cart;
    }
    
    /**
     * 更新购物车总数量和总金额
     * @param cartId 购物车ID
     */
    private void updateCartTotal(Long cartId) {
        // 获取购物车项列表
        List<CartItem> cartItems = cartItemMapper.getCartItemsByCartId(cartId);
        
        // 计算总数量和总金额
        int totalQuantity = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
        BigDecimal totalAmount = cartItems.stream()
                .map(CartItem::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        
        // 更新购物车
        Cart cart = cartMapper.selectById(cartId);
        if (cart != null) {
            cart.setTotalQuantity(totalQuantity);
            cart.setTotalAmount(totalAmount);
            cart.setUpdateTime(LocalDateTime.now());
            
            cartMapper.updateById(cart);
        }
    }
    

} 