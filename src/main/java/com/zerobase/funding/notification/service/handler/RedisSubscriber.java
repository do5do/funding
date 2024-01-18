package com.zerobase.funding.notification.service.handler;

import static com.zerobase.funding.notification.constants.Channel.CHANNEL_PREFIX;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.funding.notification.dto.NotificationDto;
import com.zerobase.funding.notification.service.SseEmitterService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SseEmitterService sseEmitterService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel())
                    .substring(CHANNEL_PREFIX.length());

            NotificationDto notificationDto = objectMapper.readValue(message.getBody(),
                    NotificationDto.class);

            sseEmitterService.sendNotificationToClient(channel, notificationDto);
        } catch (IOException e) {
            log.error("IOException is occurred. ", e);
        }
    }
}
