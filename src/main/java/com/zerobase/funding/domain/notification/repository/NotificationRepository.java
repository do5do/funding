package com.zerobase.funding.domain.notification.repository;


import com.zerobase.funding.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
