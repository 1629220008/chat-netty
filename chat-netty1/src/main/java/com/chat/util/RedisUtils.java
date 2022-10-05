package com.chat.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisUtils {
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    public void setValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }


    public void setHashValue(String key, Object entryKey, Object entryValue) {
        redisTemplate.opsForHash().put(key, entryKey, entryValue);
    }

    public Object getHashValue(String key, Object entryKey) {
        return redisTemplate.opsForHash().get(key, entryKey);
    }

}
