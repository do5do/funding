package com.zerobase.funding.notification.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {

    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public SseEmitter save(String key, SseEmitter sseEmitter) {
        emitterMap.put(key, sseEmitter);
        return sseEmitter;
    }

    public void delete(String key) {
        emitterMap.remove(key);
    }

    public Optional<SseEmitter> findById(String key) {
        return Optional.ofNullable(emitterMap.get(key));
    }
}
