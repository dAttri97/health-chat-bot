package com.curelink.test.dattri.redis;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisServiceImpl implements IRedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisServiceImpl.class);

    private final StringRedisTemplate redisTemplate;

    public RedisServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // --- String / Value ---

    @Override
    public void setValue(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Set value for key: {}", key);
        } catch (Exception e) {
            log.error("Error setting value for key: {}", key, e);
            throw e;
        }
    }

    @Override
    public void setValue(String key, String value, long timeout, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
            log.debug("Set value with expiry for key: {}", key);
        } catch (Exception e) {
            log.error("Error setting value with expiry for key: {}", key, e);
            throw e;
        }
    }

    @Override
    public String getValue(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            log.debug("Got value for key: {}", key);
            return value;
        } catch (Exception e) {
            log.error("Error getting value for key: {}", key, e);
            throw e;
        }
    }

    // --- Common ---

    @Override
    public Boolean hasKey(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            log.debug("Checked key existence: {}", key);
            return exists;
        } catch (Exception e) {
            log.error("Error checking key existence: {}", key, e);
            throw e;
        }
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Deleted key: {}", key);
        } catch (Exception e) {
            log.error("Error deleting key: {}", key, e);
            throw e;
        }
    }

    @Override
    public void expire(String key, long timeout, TimeUnit timeUnit) {
        try {
            redisTemplate.expire(key, timeout, timeUnit);
            log.debug("Set expiry for key: {}", key);
        } catch (Exception e) {
            log.error("Error setting expiry for key: {}", key, e);
            throw e;
        }
    }

    @Override
    public Boolean setIfAbsent(String key, String value, long timeout, TimeUnit timeUnit) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit);
            log.debug("setIfAbsent key: {} result: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Error in setIfAbsent for key: {}", key, e);
            throw e;
        }
    }
}
