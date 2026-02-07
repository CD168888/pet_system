package org.example.springboot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

/**
 * Jackson配置类，处理LocalDateTime序列化和反序列化问题
 */
@Configuration
public class JacksonConfig {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 注册JavaTimeModule
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // 配置LocalDateTime序列化格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(formatter);
        LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(formatter);
        
        javaTimeModule.addSerializer(java.time.LocalDateTime.class, localDateTimeSerializer);
        javaTimeModule.addDeserializer(java.time.LocalDateTime.class, localDateTimeDeserializer);
        
        // 禁用默认的时间序列化
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(javaTimeModule);
        
        return objectMapper;
    }
}
