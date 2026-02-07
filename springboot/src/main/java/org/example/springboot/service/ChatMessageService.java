package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.entity.ChatMessage;
import org.example.springboot.mapper.ChatMessageMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI聊天消息服务
 */
@Service
@Slf4j
public class ChatMessageService extends ServiceImpl<ChatMessageMapper, ChatMessage> {
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Resource
    private ObjectMapper objectMapper;
    
    private static final String CHAT_HISTORY_KEY = "chat:history:";
    private static final long CACHE_EXPIRE_HOURS = 24; // Redis缓存24小时
    
    /**
     * 保存消息（同时保存到数据库和Redis）
     */
    public ChatMessage saveMessage(Long sessionId, Long userId, Integer messageType, String content) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setMessageType(messageType);
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        
        // 保存到数据库
        save(message);
        
        // 同步到Redis缓存
        syncToRedis(sessionId);
        
        log.info("保存消息 - 会话ID: {}, 类型: {}", sessionId, messageType);
        return message;
    }
    
    /**
     * 从Redis获取聊天记录
     */
    public List<ChatMessage> getMessagesFromRedis(Long sessionId) {
        try {
            String key = CHAT_HISTORY_KEY + sessionId;
            String json = stringRedisTemplate.opsForValue().get(key);
            
            if (json != null) {
                List<ChatMessage> messages = objectMapper.readValue(json, new TypeReference<List<ChatMessage>>() {});
                log.info("从Redis获取会话 {} 的历史记录，共 {} 条消息", sessionId, messages.size());
                return messages;
            }
        } catch (Exception e) {
            log.error("从Redis读取消息失败", e);
        }
        return null;
    }
    
    /**
     * 从数据库获取聊天记录
     */
    public List<ChatMessage> getMessagesFromDB(Long sessionId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getCreateTime);
        List<ChatMessage> messages = list(wrapper);
        log.info("从数据库获取会话 {} 的历史记录，共 {} 条消息", sessionId, messages.size());
        return messages;
    }
    
    /**
     * 获取会话的所有消息（优先从Redis获取，不存在则从数据库加载并缓存）
     */
    public List<ChatMessage> getSessionMessages(Long sessionId) {
        // 先从Redis获取
        List<ChatMessage> messages = getMessagesFromRedis(sessionId);
        
        // 如果Redis没有，从数据库获取并缓存
        if (messages == null) {
            messages = getMessagesFromDB(sessionId);
            if (!messages.isEmpty()) {
                cacheToRedis(sessionId, messages);
            }
       }
       
       return messages != null ? messages : new ArrayList<>();
    }
    
    /**
     * 将消息列表缓存到Redis
     */
    private void cacheToRedis(Long sessionId, List<ChatMessage> messages) {
        try {
            String key = CHAT_HISTORY_KEY + sessionId;
            String json = objectMapper.writeValueAsString(messages);
            stringRedisTemplate.opsForValue().set(key, json, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.info("缓存会话 {} 的消息到Redis", sessionId);
        } catch (Exception e) {
            log.error("缓存消息到Redis失败", e);
        }
    }
    
    /**
     * 同步会话的所有消息到Redis（用于消息更新后）
     */
    private void syncToRedis(Long sessionId) {
        List<ChatMessage> messages = getMessagesFromDB(sessionId);
        cacheToRedis(sessionId, messages);
    }
    
    /**
     * 删除会话的所有消息（同时删除数据库和Redis）
     */
    public boolean deleteSessionMessages(Long sessionId, Long userId) {
        // 验证会话归属
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getUserId, userId);
        boolean removed = remove(wrapper);
        
        // 删除Redis缓存
        if (removed) {
            String key = CHAT_HISTORY_KEY + sessionId;
            stringRedisTemplate.delete(key);
            log.info("删除会话 {} 的所有消息", sessionId);
        }
        
        return removed;
    }
}