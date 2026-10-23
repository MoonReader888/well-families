package com.eldercare.core.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 配置：LocalDateTime/LocalDate 统一序列化格式
 *
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Bean
    public Module javaTimeModule() {
        SimpleModule module = new SimpleModule("eldercare-java-time");

        module.addSerializer(LocalDateTime.class, new JsonSerializer<>() {
            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider)
                    throws IOException {
                gen.writeString(value.format(DATETIME));
            }
        });
        module.addSerializer(LocalDate.class, new JsonSerializer<>() {
            @Override
            public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider provider)
                    throws IOException {
                gen.writeString(value.format(DATE));
            }
        });

        module.addDeserializer(LocalDateTime.class, new StdDeserializer<>(LocalDateTime.class) {
            @Override
            public LocalDateTime deserialize(JsonParser parser, DeserializationContext context)
                    throws IOException {
                String text = parser.getText().trim();
                try {
                    return LocalDateTime.parse(text, DATETIME);
                } catch (Exception e) {
                    return LocalDateTime.parse(text, DATETIME_ISO);
                }
            }
        });
        module.addDeserializer(LocalDate.class, new StdDeserializer<>(LocalDate.class) {
            @Override
            public LocalDate deserialize(JsonParser parser, DeserializationContext context)
                    throws IOException {
                String text = parser.getText().trim();
                try {
                    return LocalDate.parse(text, DATE);
                } catch (Exception e) {
                    return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
                }
            }
        });

        return module;
    }
}
