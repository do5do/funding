package com.zerobase.funding.api.common.s3.exception;

import com.zerobase.funding.global.exception.CustomException;
import com.zerobase.funding.global.exception.ErrorCode;

public class S3Exception extends CustomException {


    public S3Exception(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
