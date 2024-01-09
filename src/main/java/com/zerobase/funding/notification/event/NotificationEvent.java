package com.zerobase.funding.notification.event;

import com.zerobase.funding.domain.notification.entity.NotificationType;
import lombok.Builder;

@Builder
public record NotificationEvent(
        String memberKey,
        String message,
        NotificationType notificationType,
        String relatedUri
) {

}
