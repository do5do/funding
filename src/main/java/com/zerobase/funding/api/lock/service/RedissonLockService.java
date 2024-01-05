package com.zerobase.funding.api.lock.service;

import static com.zerobase.funding.api.exception.ErrorCode.INTERNAL_ERROR;
import static com.zerobase.funding.api.exception.ErrorCode.RESOURCE_LOCKED;

import com.zerobase.funding.api.lock.exception.LockException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedissonLockService {

    private final RedissonClient redissonClient;

    public void lock(String key, Long waitTime, Long leaseTime) {
        RLock lock = redissonClient.getLock(key);
        log.info("{} try lock...", key);

        try {
            boolean tryLock = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!tryLock) {
                log.error("lock acquisition failed.");
                throw new LockException(RESOURCE_LOCKED);
            }
        } catch (InterruptedException e) {
            throw new LockException(INTERNAL_ERROR, "lock thread is interrupted.");
        }
    }

    public void unlock(String key) {
        redissonClient.getLock(key).unlock();
        log.info("unlock {}", key);
    }
}
