package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.ChatSession;

/**
 * AI聊天会话Mapper
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

}