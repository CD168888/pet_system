package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.ChatMessage;

/**
 * AI聊天消息Mapper
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

}