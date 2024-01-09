package com.zerobase.funding.notification.repository;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, Object> cachedEvents = new ConcurrentHashMap<>();

    public SseEmitter saveEmitter(String eventId, SseEmitter sseEmitter) {
        emitters.put(eventId, sseEmitter);
        return sseEmitter;
    }

    public void saveCacheEvent(String eventId, Object data) {
        cachedEvents.put(eventId, data);
    }

    public Map<String, SseEmitter> findAllEmitters(String memberId) {
        return emitters.entrySet().stream()
                .filter(o -> o.getKey().startsWith(memberId))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    public Map<String, Object> findAllCachedEventsGtLastEventId(String memberId,
            String lastEventId) {
        return cachedEvents.entrySet().stream()
                .filter(o -> o.getKey().startsWith(memberId)
                        && o.getKey().compareTo(lastEventId) > 0) // key > lastEventId
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    public void deleteEmitter(String eventId) {
        emitters.remove(eventId);
    }

    public void deleteCachedEvent(String eventId) {
        cachedEvents.remove(eventId);
    }
}
