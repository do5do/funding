package com.zerobase.funding.api.fundingproduct.exception;

import com.zerobase.funding.api.exception.CustomException;
import com.zerobase.funding.api.exception.ErrorCode;

public class FundingProductException extends CustomException {

    public FundingProductException(ErrorCode errorCode) {
        super(errorCode);
    }
}
