package com.zerobase.funding.api.lock.aop;

import com.zerobase.funding.api.lock.annotation.DistributedLock;
import com.zerobase.funding.api.lock.service.RedissonLockService;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Aspect
public class DistributedLockAspect {

    private final RedissonLockService redissonLockService;
    private final LockCallNewTransaction lockCallNewTransaction;

    @Around("@annotation(com.zerobase.funding.api.lock.annotation.DistributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        DistributedLock annotation = method.getAnnotation(DistributedLock.class);
        String key = getLockKey(joinPoint, annotation);
        redissonLockService.lock(key, annotation.waitTime(), annotation.leasTime());

        try {
            return lockCallNewTransaction.proceed(joinPoint);
        } finally {
            redissonLockService.unlock(key);
        }
    }

    private static String getLockKey(ProceedingJoinPoint joinPoint, DistributedLock annotation)
            throws NoSuchFieldException, IllegalAccessException {
        Object obj = joinPoint.getArgs()[0];
        Field field = obj.getClass().getDeclaredField(annotation.idField());
        field.trySetAccessible();
        String value = field.get(obj).toString();

        return annotation.keyPrefix() + value;
    }
}
