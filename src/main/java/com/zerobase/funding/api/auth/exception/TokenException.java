package com.zerobase.funding.api.auth.exception;

import com.zerobase.funding.api.exception.CustomException;
import com.zerobase.funding.api.exception.ErrorCode;

public class TokenException extends CustomException {

    public TokenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
