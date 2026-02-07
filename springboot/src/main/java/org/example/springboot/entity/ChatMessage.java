package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI聊天消息实体类
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 消息类型：0=AI消息，1=用户消息
     */
    private Integer messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}