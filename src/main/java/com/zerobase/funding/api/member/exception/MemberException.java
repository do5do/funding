package com.zerobase.funding.api.member.exception;

import com.zerobase.funding.global.exception.CustomException;
import com.zerobase.funding.global.exception.ErrorCode;

public class MemberException extends CustomException {

    public MemberException(ErrorCode errorCode) {
        super(errorCode);
    }
}
