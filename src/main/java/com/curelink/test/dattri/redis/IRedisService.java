package com.curelink.test.dattri.redis;

import java.util.concurrent.TimeUnit;

public interface IRedisService {

    // --- String / Value ---

    void setValue(String key, String value);

    void setValue(String key, String value, long timeout, TimeUnit timeUnit);

    String getValue(String key);

    // --- Common ---

    Boolean hasKey(String key);

    void delete(String key);

    void expire(String key, long timeout, TimeUnit timeUnit);

    /**
     * Set key only if it does not already exist.
     * Returns true if the key was set, false if it already existed.
     */
    Boolean setIfAbsent(String key, String value, long timeout, TimeUnit timeUnit);
}
