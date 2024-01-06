package com.zerobase.funding.notification.service;

import com.zerobase.funding.notification.constants.MsgFormat;
import com.zerobase.funding.notification.repository.SseEmitterRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final SseEmitterRepository sseEmitterRepository;

    @Value("${sse.timeout}")
    private Long timeout;

    public SseEmitter subscribe(Long id) {
        String key = String.valueOf(id);
        SseEmitter sseEmitter = new SseEmitter(timeout);
        sseEmitterRepository.save(key, sseEmitter);

        sseEmitter.onTimeout(sseEmitter::complete);
        sseEmitter.onError((e) -> sseEmitter.complete());
        sseEmitter.onCompletion(() -> sseEmitterRepository.delete(key));

        send(String.format(MsgFormat.SUBSCRIBE, key), sseEmitter, "subscribe");
        return sseEmitter;
    }

    @Async
    public void sendMessage(Long id, String msg, String name) {
        String key = String.valueOf(id);
        sseEmitterRepository.findById(key)
                .ifPresent(emitter -> {
                    send(msg, emitter, name);
                    sseEmitterRepository.delete(key);
                    log.info("send message to subscriber[{}] successful", key);
                });
    }

    private void send(String msg, SseEmitter sseEmitter, String name) {
        try {
            log.info("send {}", msg);
            sseEmitter.send(SseEmitter.event()
                    .name(name)
                    .data(msg));
        } catch (IOException e) {
            log.error("IOException is occurred", e);
        }
    }
}
