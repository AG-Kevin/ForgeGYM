package com.myidea.gym.lock;

public interface LockService {
    String tryLock(String key, long ttlMs);

    void unlock(String key, String token);
}
