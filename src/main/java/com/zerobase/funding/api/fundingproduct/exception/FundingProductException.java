package com.zerobase.funding.api.fundingproduct.exception;

import com.zerobase.funding.global.exception.CustomException;
import com.zerobase.funding.global.exception.ErrorCode;

public class FundingProductException extends CustomException {

    public FundingProductException(ErrorCode errorCode) {
        super(errorCode);
    }
}
