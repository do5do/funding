package com.zerobase.funding.notification.service;

import static com.zerobase.funding.api.exception.ErrorCode.NOTIFICATION_NOT_FOUND;

import com.zerobase.funding.api.auth.service.AuthenticationService;
import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.notification.entity.Notification;
import com.zerobase.funding.domain.notification.repository.NotificationRepository;
import com.zerobase.funding.notification.constants.MsgFormat;
import com.zerobase.funding.notification.dto.NotificationDto;
import com.zerobase.funding.notification.event.NotificationEvent;
import com.zerobase.funding.notification.exception.NotificationException;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuthenticationService authenticationService;
    private final SseEmitterService sseEmitterService;
    private final RedisMessageService redisMessageService;

    public SseEmitter subscribe(String memberKey) {
        SseEmitter sseEmitter = sseEmitterService.createEmitter(memberKey);
        sseEmitterService.send(MsgFormat.SUBSCRIBE, memberKey, sseEmitter); // send dummy

        redisMessageService.subscribe(memberKey);

        sseEmitter.onTimeout(sseEmitter::complete);
        sseEmitter.onError((e) -> sseEmitter.complete());
        sseEmitter.onCompletion(() -> {
            sseEmitterService.deleteEmitter(memberKey);
            redisMessageService.removeSubscribe(memberKey);
        });
        return sseEmitter;
    }

    @Transactional
    public void sendNotification(NotificationEvent event) {
        Member member = authenticationService.getMemberOrThrow(event.memberKey());
        Notification notification =
                Notification.of(event.message(), event.notificationType(), event.relatedUri());
        notification.addMember(member);
        notificationRepository.save(notification);

        redisMessageService.publish(event.memberKey(), NotificationDto.fromEntity(notification));
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
