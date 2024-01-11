package com.zerobase.funding.notification.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter save(String eventId, SseEmitter sseEmitter) {
        emitters.put(eventId, sseEmitter);
        return sseEmitter;
    }

    public Optional<SseEmitter> findById(String memberId) {
        return Optional.ofNullable(emitters.get(memberId));
    }

    public void deleteById(String eventId) {
        emitters.remove(eventId);
    }
}
