package com.zerobase.funding.api.exception;

public record ErrorResponse(
        ErrorCode errorCode,
        String message
) {

}
