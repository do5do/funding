package com.zerobase.funding.domain.notification.repository;


import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.notification.entity.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByMemberAndIsRead(Member member, boolean isRead);
}
