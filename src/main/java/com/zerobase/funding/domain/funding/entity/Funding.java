package com.zerobase.funding.domain.funding.entity;

import com.zerobase.funding.domain.common.entity.BaseTimeEntity;
import com.zerobase.funding.domain.delivery.entity.Delivery;
import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.reward.entity.Reward;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_member_id_reward_id",
        columnNames = {"member_id", "reward_id"}))
public class Funding extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private Integer fundingPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id")
    private Reward reward;

    @Builder
    public Funding(Status status, Integer fundingPrice) {
        this.status = status;
        this.fundingPrice = fundingPrice;
    }

    public void addMember(Member member) {
        this.member = member;
    }

    public void addReward(Reward reward) {
        this.reward = reward;
    }

    public void addDelivery(Delivery delivery) {
        this.delivery = delivery;
    }
}
