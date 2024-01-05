package com.zerobase.funding.api.lock.exception;

import com.zerobase.funding.api.exception.CustomException;
import com.zerobase.funding.api.exception.ErrorCode;

public class LockException extends CustomException {

    public LockException(ErrorCode errorCode) {
        super(errorCode);
    }

    public LockException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
