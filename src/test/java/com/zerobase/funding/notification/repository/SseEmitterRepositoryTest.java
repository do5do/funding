package com.zerobase.funding.notification.repository;

import static com.zerobase.funding.domain.notification.entity.NotificationType.FUNDING_ENDED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zerobase.funding.notification.dto.NotificationDto;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseEmitterRepositoryTest {

    SseEmitterRepository sseEmitterRepository;

    String memberKey = "d6c62de89f4e4d4faa3e643a743e9311";

    @BeforeEach
    void setup() {
        sseEmitterRepository = new SseEmitterRepository();
    }

    @Test
    @DisplayName("LastEventId보다 큰 id 값만 반환한다.")
    void findAllCachedEventsGtLastEventId() {
        // given
        String lastEventId = memberKey + "_" + System.currentTimeMillis();

        for (int i = 1; i <= 3; i++) {
            String eventId = memberKey + "_" + (System.currentTimeMillis() + i);
            sseEmitterRepository.saveCacheEvent(eventId, new NotificationDto("test" + i,
                    FUNDING_ENDED, "uri", false));
        }

        // when
        Map<String, Object> cachedEvents = sseEmitterRepository.findAllCachedEventsGtLastEventId(
                memberKey, lastEventId);

        // then
        assertEquals(3, cachedEvents.keySet().size());
        assertTrue(cachedEvents.entrySet().stream()
                .allMatch(o -> o.getKey().compareTo(lastEventId) > 0));
    }

    @Test
    @DisplayName("memberId로 시작하는 값을 반환한다.")
    void findAllEmitters() {
        // given
        for (int i = 0; i < 3; i++) {
            String eventId = memberKey + "_" + (System.currentTimeMillis() + i);
            sseEmitterRepository.saveEmitter(eventId, new SseEmitter());
        }

        // when
        Map<String, SseEmitter> emitters = sseEmitterRepository.findAllEmitters(memberKey);

        // then
        assertEquals(3, emitters.keySet().size());
        assertTrue(emitters.entrySet().stream()
                .allMatch(o -> o.getKey().startsWith(memberKey)));
    }
}