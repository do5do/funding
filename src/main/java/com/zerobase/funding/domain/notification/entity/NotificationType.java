package com.zerobase.funding.domain.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    FUNDING_ENDED("펀딩 종료 시점 알림");

    private final String description;
}
