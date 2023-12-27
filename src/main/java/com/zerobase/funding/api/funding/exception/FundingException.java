package com.zerobase.funding.api.funding.exception;

import com.zerobase.funding.api.exception.CustomException;
import com.zerobase.funding.api.exception.ErrorCode;

public class FundingException extends CustomException {

    public FundingException(ErrorCode errorCode) {
        super(errorCode);
    }
}
