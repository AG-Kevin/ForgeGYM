package com.myidea.gym.lock;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Profile("!local")
@RequiredArgsConstructor
public class RedisLockService implements LockService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public String tryLock(String key, long ttlMs) {
        String token = java.util.UUID.randomUUID().toString();
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(key, token, Duration.ofMillis(ttlMs));
        return (ok != null && ok) ? token : null;
    }

    @Override
    public void unlock(String key, String token) {
        String val = stringRedisTemplate.opsForValue().get(key);
        if (token != null && token.equals(val)) {
            stringRedisTemplate.delete(key);
        }
    }
}
