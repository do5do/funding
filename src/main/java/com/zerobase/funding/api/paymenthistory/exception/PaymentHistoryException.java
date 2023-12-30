package com.zerobase.funding.api.paymenthistory.exception;

import com.zerobase.funding.api.exception.CustomException;
import com.zerobase.funding.api.exception.ErrorCode;

public class PaymentHistoryException extends CustomException {

    public PaymentHistoryException(ErrorCode errorCode) {
        super(errorCode);
    }
}
