package com.zerobase.funding.notification.service;

import static com.zerobase.funding.notification.constants.Channel.CHANNEL_PREFIX;

import com.zerobase.funding.notification.dto.NotificationDto;
import com.zerobase.funding.notification.service.handler.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RedisMessageService {

    private final RedisMessageListenerContainer container;
    private final RedisSubscriber subscriber;
    private final RedisTemplate<String, Object> redisTemplate;

    public void subscribe(String channel) {
        container.addMessageListener(subscriber, ChannelTopic.of(getChannelName(channel)));
    }

    public void publish(String channel, NotificationDto notificationDto) {
        redisTemplate.convertAndSend(getChannelName(channel), notificationDto);
    }

    public void removeSubscribe(String channel) {
        container.removeMessageListener(subscriber, ChannelTopic.of(getChannelName(channel)));
    }

    private String getChannelName(String id) {
        return CHANNEL_PREFIX + id;
    }
}
