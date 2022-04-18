package com.wsz.seckill.config;

/**
 * @author : wanshanzhe
 * @date : 2022/4/9 5:37 下午
 * @desc :
 */

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 格式化LocalDateTime的格式化配置类  配置该配置类，可以将我们的日期格式化为我们想要的格式
 */
@Configuration
public class LocalDateTimeSerializerConfig {

        @Value("${spring.jackson.date-format}")
        private String pattern;

        public LocalDateTimeSerializer localDateTimeDeserializer() {
            return new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(pattern));
        }

        @Bean
        public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
            return builder -> builder.serializerByType(LocalDateTime.class, localDateTimeDeserializer());
    }
}
