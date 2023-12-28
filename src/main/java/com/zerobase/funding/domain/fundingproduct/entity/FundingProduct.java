package com.zerobase.funding.domain.fundingproduct.entity;

import com.zerobase.funding.api.fundingproduct.dto.Edit;
import com.zerobase.funding.domain.common.entity.BaseTimeEntity;
import com.zerobase.funding.domain.image.entity.Image;
import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.reward.entity.Reward;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class FundingProduct extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, length = 100)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer targetAmount;

    @ColumnDefault("0")
    private Integer views;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "fundingProduct", cascade = CascadeType.ALL)
    private List<Reward> rewards = new ArrayList<>();

    @OneToMany(mappedBy = "fundingProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @Builder
    public FundingProduct(String title, String description, LocalDate startDate, LocalDate endDate,
            Integer targetAmount) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.targetAmount = targetAmount;
    }

    public void addMember(Member member) {
        this.member = member;
    }

    public void addRewards(Reward reward) {
        rewards.add(reward);
        reward.setFundingProduct(this);
    }

    public void addImages(Image image) {
        images.add(image);
        image.setFundingProduct(this);
    }

    public FundingProduct setViews(Integer views) {
        this.views = views;
        return this;
    }

    public void updateFundingProduct(Edit.Request request) {
        this.title = request.title();
        this.description = request.description();
        this.startDate = request.startDate();
        this.endDate = request.endDate();
        this.targetAmount = request.targetAmount();
    }
}
