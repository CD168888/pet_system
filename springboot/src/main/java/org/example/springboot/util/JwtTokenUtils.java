package org.example.springboot.util;

import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.example.springboot.entity.User;
import org.example.springboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

/**
 * JWT令牌工具类
 * 用于生成、解析和验证JWT令牌
 */
@Component
public class JwtTokenUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenUtils.class);
    private static UserService staticUserService;
    
    @Resource
    private UserService userService;
    
    /**
     * 初始化静态UserService
     */
    @PostConstruct
    public void setUserService() {
        staticUserService = userService;
    }
    
    /**
     * 生成JWT令牌
     * 
     * @param userId 用户ID
     * @param sign   签名密钥
     * @return JWT令牌字符串
     */
    public static String genToken(String userId, String sign) {
        return JWT.create()
                .withAudience(userId)  // 将用户ID作为JWT的audience
                .withExpiresAt(DateUtil.offsetHour(new Date(), 2))  // 设置2小时过期
                .sign(Algorithm.HMAC256(sign));  // 使用HMAC256算法签名
    }
    
    /**
     * 从请求中获取JWT令牌
     * 
     * @return JWT令牌字符串，若未找到则返回null
     */
    private static String getTokenFromRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        if (request == null) {
            LOGGER.error("获取HttpServletRequest失败");
            return null;
        }
        
        // 优先从请求头获取token
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            // 其次从请求参数获取token
            token = request.getParameter("token");
        }
        
        return token;
    }
    
    /**
     * 获取当前登录用户的ID
     * 
     * @return 当前用户ID，若获取失败则返回null
     */
    public static Long getCurrentUserId() {
        String token = null;
        try {
            token = getTokenFromRequest();
            if (StringUtils.isBlank(token)) {
                LOGGER.error("获取当前登录的token失败，token: {}", token);
                return null;
            }
            
            // 解析token获取用户ID
            String userIdStr = JWT.decode(token).getAudience().get(0);
            return Long.valueOf(userIdStr);
        } catch (Exception e) {
            LOGGER.error("获取当前用户ID失败，token: {}", token, e);
            return null;
        }
    }
    
    /**
     * 获取当前登录用户信息
     * 
     * @return 当前用户对象，若获取失败则返回null
     */
    public static User getCurrentUser() {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return null;
            }
            
            return staticUserService.getUserById(userId);
        } catch (Exception e) {
            LOGGER.error("获取当前用户信息失败", e);
            return null;
        }
    }
}
