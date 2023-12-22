package com.zerobase.funding.api.auth.exception;

import com.zerobase.funding.global.exception.CustomException;
import com.zerobase.funding.global.exception.ErrorCode;

public class AuthException extends CustomException {

    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }
}
