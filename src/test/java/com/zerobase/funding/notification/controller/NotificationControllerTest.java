package com.zerobase.funding.notification.controller;

import static com.zerobase.funding.domain.notification.entity.NotificationType.FUNDING_ENDED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.funding.api.auth.jwt.TokenProvider;
import com.zerobase.funding.notification.dto.NotificationDto;
import com.zerobase.funding.notification.service.NotificationService;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @MockBean
    NotificationService notificationService;

    @MockBean
    TokenProvider tokenProvider;

    @Autowired
    WebApplicationContext applicationContext;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .build();
    }

    String uri = "/notification";
    NotificationDto notificationDto = new NotificationDto("test", FUNDING_ENDED,
            "relatedUri", false);

    @Test
    @DisplayName("SSE 구독하기")
    @WithMockUser(username = "kim")
    void subscribe() throws Exception {
        // given
        SseEmitter sseEmitter = new SseEmitter();
        String id = "id_test";

        sseEmitter.send(SseEmitter.event()
                .id(id)
                .data(notificationDto, MediaType.APPLICATION_JSON));

        given(notificationService.subscribe(any(), any()))
                .willReturn(sseEmitter);

        // when
        // then
        mockMvc.perform(get(uri + "/subscribe"))
                .andExpect(status().isOk())
                .andExpect(content().string("id:" + id + "\n" +
                        "data:" + objectMapper.writeValueAsString(notificationDto) + "\n\n"))
                .andDo(print());
    }

    @Test
    @DisplayName("알림 조회 시 연관 uri로 리다이렉트")
    void redirect() throws Exception {
        // given
        String redirectUri = "/endpoint/1";
        given(notificationService.getRedirectUri(any()))
                .willReturn(URI.create(redirectUri));

        // when
        // then
        mockMvc.perform(get(uri + "/1"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", redirectUri))
                .andDo(print());
    }

    @Test
    @DisplayName("알림 목록 조회")
    @WithMockUser(username = "kim")
    void notifications() throws Exception {
        // given
        given(notificationService.notifications(any()))
                .willReturn(List.of(notificationDto));

        // when
        // then
        mockMvc.perform(get(uri))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("test"))
                .andExpect(jsonPath("$[0].notificationType")
                        .value(FUNDING_ENDED.name()))
                .andExpect(jsonPath("$[0].relatedUri")
                        .value("relatedUri"))
                .andExpect(jsonPath("$[0].isRead").value("false"))
                .andDo(print());
    }
}