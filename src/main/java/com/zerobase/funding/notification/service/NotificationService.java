package com.zerobase.funding.notification.service;

import static com.zerobase.funding.api.exception.ErrorCode.NOTIFICATION_NOT_FOUND;

import com.zerobase.funding.api.auth.service.AuthenticationService;
import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.notification.entity.Notification;
import com.zerobase.funding.domain.notification.entity.NotificationType;
import com.zerobase.funding.domain.notification.repository.NotificationRepository;
import com.zerobase.funding.notification.constants.MsgFormat;
import com.zerobase.funding.notification.dto.NotificationDto;
import com.zerobase.funding.notification.exception.NotificationException;
import com.zerobase.funding.notification.repository.SseEmitterRepository;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final SseEmitterRepository sseEmitterRepository;
    private final AuthenticationService authenticationService;
    private final NotificationRepository notificationRepository;

    @Value("${sse.timeout}")
    private Long timeout;

    public SseEmitter subscribe(String memberKey, String lastEventId) {
        String emitterKey = getCurrentId(memberKey);
        SseEmitter sseEmitter = sseEmitterRepository.saveEmitter(emitterKey,
                new SseEmitter(timeout));

        sseEmitter.onTimeout(sseEmitter::complete);
        sseEmitter.onError((e) -> sseEmitter.complete());
        sseEmitter.onCompletion(() -> sseEmitterRepository.deleteEmitter(emitterKey));

        // send dummy data
        send(emitterKey, MsgFormat.SUBSCRIBE, emitterKey, sseEmitter);

        if (StringUtils.hasText(lastEventId)) { // 미수신한 이벤트 전송
            sseEmitterRepository.findAllCachedEventsGtLastEventId(memberKey, lastEventId)
                    .forEach((key, val) -> {
                        send(key, val, emitterKey, sseEmitter);
                        sseEmitterRepository.deleteCachedEvent(key);
                    });
        }
        return sseEmitter;
    }

    @Transactional
    public void sendNotification(String memberKey, String message,
            NotificationType notificationType, String relatedUri) {
        NotificationDto notificationDto = saveNotification(memberKey, message, notificationType,
                relatedUri);

        String eventId = getCurrentId(memberKey);
        sseEmitterRepository.saveCacheEvent(eventId, notificationDto); // caching event

        sseEmitterRepository.findAllEmitters(memberKey)
                .forEach((key, val) -> {
                    send(eventId, notificationDto, key, val);
                    sseEmitterRepository.deleteEmitter(key);
                });
    }

    private NotificationDto saveNotification(String memberKey, String message,
            NotificationType notificationType, String relatedUri) {
        Member member = authenticationService.getMemberOrThrow(memberKey);
        Notification notification = Notification.of(message, notificationType, relatedUri);
        notification.addMember(member);
        return NotificationDto.fromEntity(notificationRepository.save(notification));
    }

    private void send(String eventId, Object data, String emitterKey, SseEmitter sseEmitter) {
        try {
            log.info("send to client {}:[{}]", eventId, data);
            sseEmitter.send(SseEmitter.event()
                    .id(eventId)
                    .data(data, MediaType.APPLICATION_JSON));
        } catch (IOException | IllegalStateException e) {
            log.error("IOException | IllegalStateException is occurred", e);
            sseEmitterRepository.deleteEmitter(emitterKey);
        }
    }

    private String getCurrentId(String memberKey) {
        return memberKey + "_" + System.currentTimeMillis();
    }

    @Transactional
    public URI getRedirectUri(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(NOTIFICATION_NOT_FOUND));
        notification.setRead();
        return URI.create(notification.getRelatedUri());
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> notifications(String memberKey) {
        Member member = authenticationService.getMemberOrThrow(memberKey);
        return notificationRepository.findAllByMemberAndIsRead(member, false)
                .stream().map(NotificationDto::fromEntity)
                .toList();
    }
}
