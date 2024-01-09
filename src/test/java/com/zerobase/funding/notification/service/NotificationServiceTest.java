package com.zerobase.funding.notification.service;

import static com.zerobase.funding.api.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.zerobase.funding.api.exception.ErrorCode.NOTIFICATION_NOT_FOUND;
import static com.zerobase.funding.domain.notification.entity.NotificationType.FUNDING_ENDED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

import com.zerobase.funding.api.auth.exception.AuthException;
import com.zerobase.funding.api.auth.service.AuthenticationService;
import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.notification.entity.Notification;
import com.zerobase.funding.domain.notification.entity.NotificationType;
import com.zerobase.funding.domain.notification.repository.NotificationRepository;
import com.zerobase.funding.notification.dto.NotificationDto;
import com.zerobase.funding.notification.exception.NotificationException;
import com.zerobase.funding.notification.repository.SseEmitterRepository;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    SseEmitterRepository sseEmitterRepository;

    @Mock
    AuthenticationService authenticationService;

    @Mock
    NotificationRepository notificationRepository;

    @InjectMocks
    NotificationService notificationService;

    String memberKey = "key";

    @Nested
    @DisplayName("SSE 구독 메서드")
    class SubscribeMethod {

        @Test
        @DisplayName("성공 - lastEventId가 null인 경우")
        void subscribe() {
            // given
            given(sseEmitterRepository.saveEmitter(any(), any()))
                    .willReturn(new SseEmitter());

            // when
            // then
            assertDoesNotThrow(() -> notificationService.subscribe(memberKey, null));
        }

        @Test
        @DisplayName("성공 - lastEventId가 있고 캐시 이벤트가 경우")
        void subscribe_lastEventId() {
            // given
            given(sseEmitterRepository.saveEmitter(any(), any()))
                    .willReturn(new SseEmitter());

            HashMap<String, Object> cached = new HashMap<>();
            cached.put(memberKey, new NotificationDto("test", FUNDING_ENDED,
                    "relatedUri", false));

            given(sseEmitterRepository.findAllCachedEventsGtLastEventId(any(), any()))
                    .willReturn(cached);

            doNothing().when(sseEmitterRepository).deleteCachedEvent(any());

            // when
            // then
            assertDoesNotThrow(() ->
                    notificationService.subscribe(memberKey, "lastId"));
        }
    }

    @Nested
    @DisplayName("알림 발송 메서드")
    class SendNotificationMethod {

        String message = "message";
        NotificationType notificationType = FUNDING_ENDED;
        String relatedUri = "/endpoint/1";

        @Test
        @DisplayName("성공")
        void sendNotification() {
            // given
            given(authenticationService.getMemberOrThrow(any()))
                    .willReturn(Member.builder().build());

            given(notificationRepository.save(any()))
                    .willReturn(Notification.of(message, notificationType, relatedUri));

            doNothing().when(sseEmitterRepository).saveCacheEvent(any(), any());

            Map<String, SseEmitter> emitters = new HashMap<>() {{
                put(memberKey, new SseEmitter());
            }};

            given(sseEmitterRepository.findAllEmitters(any()))
                    .willReturn(emitters);

            // when
            // then
            assertDoesNotThrow(() -> notificationService.sendNotification(
                    memberKey, message, notificationType, relatedUri));
        }

        @Test
        @DisplayName("실패 - 회원이 없는 경우 알림을 보낼 수 없다.")
        void sendNotification_member_not_found() {
            // given
            given(authenticationService.getMemberOrThrow(any()))
                    .willThrow(new AuthException(MEMBER_NOT_FOUND));

            // when
            AuthException exception = assertThrows(AuthException.class,
                    () -> notificationService.sendNotification(
                            memberKey, message, notificationType, relatedUri));

            // then
            assertEquals(MEMBER_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("알림 조회 메서드")
    class GetRedirectUriMethod {

        @Test
        @DisplayName("성공 - 알림의 읽음 여부가 true로 변경된다.")
        void getRedirectUri() {
            // given
            String redirectUri = "/endpoint/1";

            Notification notification = Notification.of("message", FUNDING_ENDED,
                    redirectUri);

            given(notificationRepository.findById(any()))
                    .willReturn(Optional.of(notification));

            // when
            URI uri = notificationService.getRedirectUri(1L);

            // then
            assertEquals(redirectUri, uri.toString());
            assertTrue(notification.isRead());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 알림인 경우 조회할 수 없다.")
        void getRedirectUri_notification_not_found() {
            // given
            given(notificationRepository.findById(any()))
                    .willThrow(new NotificationException(NOTIFICATION_NOT_FOUND));

            // when
            NotificationException exception = assertThrows(NotificationException.class,
                    () ->
                            notificationService.getRedirectUri(1L));

            // then
            assertEquals(NOTIFICATION_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("알림 목록 조회 메서드")
    class NotificationsMethod {

        @Test
        @DisplayName("성공 - 읽지 않은 목록을 반환한다.")
        void notifications() {
            // given
            given(authenticationService.getMemberOrThrow(any()))
                    .willReturn(Member.builder().build());

            Notification notification = Notification.of("message", FUNDING_ENDED,
                    "redirectUri");

            given(notificationRepository.findAllByMemberAndIsRead(any(), anyBoolean()))
                    .willReturn(List.of(notification));

            // when
            List<NotificationDto> notifications = notificationService.notifications(memberKey);

            // then
            assertFalse(notifications.stream().allMatch(NotificationDto::isRead));
            assertEquals(1, notifications.size());
        }

        @Test
        @DisplayName("실패 - 회원이 없는 경우 알림 목록을 조회할 수 없다.")
        void notifications_member_not_found() {
            // given
            given(authenticationService.getMemberOrThrow(any()))
                    .willThrow(new AuthException(MEMBER_NOT_FOUND));

            // when
            AuthException exception = assertThrows(AuthException.class,
                    () -> notificationService.notifications(memberKey));

            // then
            assertEquals(MEMBER_NOT_FOUND, exception.getErrorCode());
        }
    }
}