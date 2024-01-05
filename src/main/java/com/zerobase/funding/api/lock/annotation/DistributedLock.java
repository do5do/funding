package com.zerobase.funding.api.lock.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ METHOD, TYPE })
@Retention(RUNTIME)
public @interface DistributedLock {
    String keyPrefix();

    String idField();

    long waitTime() default 5L;

    long leasTime() default 3L;
}
