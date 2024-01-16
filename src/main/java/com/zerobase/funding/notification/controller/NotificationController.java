package com.zerobase.funding.notification.controller;

import static com.zerobase.funding.api.exception.ErrorCode.INVALID_REQUEST;

import com.zerobase.funding.api.auth.annotaion.RoleUser;
import com.zerobase.funding.notification.dto.NotificationDto;
import com.zerobase.funding.notification.exception.NotificationException;
import com.zerobase.funding.notification.service.NotificationService;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequestMapping("/notification")
@RequiredArgsConstructor
@RestController
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * SSE 구독
     *
     * @param userDetails 인증 유저
     * @return sseEmitter
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (ObjectUtils.isEmpty(userDetails)) {
            throw new NotificationException(INVALID_REQUEST, "연결 시 인증이 필요합니다.");
        }
        return ResponseEntity.ok(notificationService.subscribe(userDetails.getUsername()));
    }

    /**
     * 알림 조회
     *
     * @param id 알림 아이디
     * @return redirect
     */
    @RoleUser
    @GetMapping("/{id}")
    public ResponseEntity<Void> redirect(@PathVariable Long id) {
        URI redirectUri = notificationService.getRedirectUri(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(redirectUri);
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    /**
     * 알림 목록 조회
     *
     * @param userDetails 인증 유저
     * @return 읽지 않은 알림 목록
     */
    @RoleUser
    @GetMapping
    public ResponseEntity<List<NotificationDto>> notifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(notificationService.notifications(userDetails.getUsername()));
    }
}
