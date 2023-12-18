package com.zerobase.funding.api.member.exception;

import com.zerobase.funding.api.exception.CustomException;
import com.zerobase.funding.api.exception.ErrorCode;

public class MemberException extends CustomException {

    public MemberException(ErrorCode errorCode) {
        super(errorCode);
    }
}
