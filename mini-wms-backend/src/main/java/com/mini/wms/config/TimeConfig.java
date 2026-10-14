package com.mini.wms.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

/**
 * 时间与 JSON 序列化统一配置
 *
 * - 全系统时间使用上海时区（Asia/Shanghai），在启动类与容器 TZ 中设置默认时区；
 * - 所有 LocalDateTime 字段以「yyyy-MM-dd HH:mm:ss」（精确到秒，无毫秒/纳秒）序列化与反序列化。
 */
@Configuration
public class TimeConfig {

    /** 统一时间格式：精确到秒 */
    public static final String PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(PATTERN);

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer localDateTimeCustomizer() {
        return builder -> {
            builder.serializers(new LocalDateTimeSerializer(FORMATTER));
            builder.deserializers(new LocalDateTimeDeserializer(FORMATTER));
        };
    }
}
