package com.zerobase.funding.notification.dto;

import com.zerobase.funding.domain.notification.entity.Notification;
import com.zerobase.funding.domain.notification.entity.NotificationType;
import lombok.Builder;

@Builder
public record NotificationDto(
        String message,
        NotificationType notificationType,
        String relatedUri,
        boolean isRead
) {

    public static NotificationDto fromEntity(Notification notification) {
        return NotificationDto.builder()
                .message(notification.getMessage())
                .notificationType(notification.getNotificationType())
                .relatedUri(notification.getRelatedUri())
                .isRead(notification.isRead())
                .build();
    }
}
