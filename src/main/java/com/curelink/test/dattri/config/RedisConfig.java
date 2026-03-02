package com.curelink.test.dattri.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis client configuration.
 * Spring Boot auto-configures Redis from application.properties and provides:
 * - {@link org.springframework.data.redis.connection.RedisConnectionFactory}
 * - {@link org.springframework.data.redis.core.StringRedisTemplate} for string key/value ops
 * - {@link org.springframework.data.redis.core.RedisTemplate} for generic ops
 * Inject {@link org.springframework.data.redis.core.StringRedisTemplate} where needed.
 */
@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

}
