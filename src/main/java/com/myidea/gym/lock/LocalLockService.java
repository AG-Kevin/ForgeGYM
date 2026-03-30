package com.myidea.gym.lock;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Profile("local")
public class LocalLockService implements LockService {

    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Override
    public String tryLock(String key, long ttlMs) {
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        boolean ok = lock.tryLock();
        return ok ? UUID.randomUUID().toString() : null;
    }

    @Override
    public void unlock(String key, String token) {
        ReentrantLock lock = locks.get(key);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
