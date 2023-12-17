package com.zerobase.funding.api.auth.dto;

import static com.zerobase.funding.api.exception.ErrorCode.ILLEGAL_REGISTRATION_ID;

import com.zerobase.funding.api.auth.exception.AuthException;
import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.member.entity.Role;
import com.zerobase.funding.global.utils.KeyGenerator;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2UserInfo {

    private String name;
    private String email;
    private String profile;

    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "google" ->
                    ofGoogle(attributes);
            case "kakao" ->
                    ofKakao(attributes);
            default ->
                    throw new AuthException(ILLEGAL_REGISTRATION_ID);
        };
    }

    private static OAuth2UserInfo ofGoogle(Map<String, Object> attributes) {
        return OAuth2UserInfo.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .profile((String) attributes.get("picture"))
                .build();
    }

    private static OAuth2UserInfo ofKakao(Map<String, Object> attributes) {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) account.get("profile");

        return OAuth2UserInfo.builder()
                .name((String) profile.get("nickname"))
                .email((String) account.get("email"))
                .profile((String) profile.get("profile_image_url"))
                .build();
    }

    public Member toEntity() {
        return Member.builder()
                .name(name)
                .email(email)
                .profile(profile)
                .memberKey(KeyGenerator.generateKey())
                .role(Role.USER)
                .build();
    }
}
