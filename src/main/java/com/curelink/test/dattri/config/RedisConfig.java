package com.curelink.test.dattri.config;

import org.springframework.context.annotation.Configuration;

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
}
