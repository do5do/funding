package com.zerobase.funding.api.reward.exception;

import com.zerobase.funding.api.exception.CustomException;
import com.zerobase.funding.api.exception.ErrorCode;

public class RewardException extends CustomException {

    public RewardException(ErrorCode errorCode) {
        super(errorCode);
    }
}
