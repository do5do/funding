package com.zerobase.funding.api.exception;

import static org.springframework.http.HttpStatus.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // auth
    ILLEGAL_REGISTRATION_ID(NOT_ACCEPTABLE, "illegal registration id"),
    TOKEN_EXPIRED(UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_TOKEN(UNAUTHORIZED, "올바르지 않은 토큰입니다."),
    INVALID_JWT_SIGNATURE(UNAUTHORIZED, "잘못된 JWT 시그니처입니다."),

    // member
    MEMBER_NOT_FOUND(NOT_FOUND, "회원을 찾을 수 없습니다."),

    // funding product
    FUNDING_PRODUCT_NOT_FOUND(NOT_FOUND, "펀딩 상품을 찾을 수 없습니다."),
    INVALID_DATE(BAD_REQUEST, "시작 날짜는 완료 날짜 이전이어야 합니다."),
    FUNDING_PRODUCT_NOT_EDIT(BAD_REQUEST, "펀딩 진행중인 상품은 수정할 수 없습니다."),
    FUNDING_PRODUCT_NOT_DELETE(BAD_REQUEST, "펀딩 진행중인 상품은 삭제할 수 없습니다."),

    // funding
    FUNDING_NOT_FOUND(NOT_FOUND, "펀딩을 찾을 수 없습니다."),

    // global
    NO_ACCESS(FORBIDDEN, "접근 권한이 없습니다."),
    RESOURCE_NOT_FOUND(NOT_FOUND, "요청한 자원을 찾을 수 없습니다."),
    INVALID_REQUEST(BAD_REQUEST, "올바르지 않은 요청입니다."),
    INTERNAL_ERROR(INTERNAL_SERVER_ERROR, "예상치못한 에러가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
