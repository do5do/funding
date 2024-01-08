package com.zerobase.funding.domain.notification.entity;

import com.zerobase.funding.domain.common.entity.BaseTimeEntity;
import com.zerobase.funding.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private String relatedUri;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean read = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // receiver

    @Builder
    public Notification(String message, EventType eventType, String relatedUri) {
        this.message = message;
        this.eventType = eventType;
        this.relatedUri = relatedUri;
    }

    public void addMember(Member member) {
        this.member = member;
    }

    public void setRead() {
        this.read = true;
    }
}
