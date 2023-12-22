package com.zerobase.funding.global.utils.s3.exception;

import com.zerobase.funding.api.exception.CustomException;
import com.zerobase.funding.api.exception.ErrorCode;

public class S3Exception extends CustomException {


    public S3Exception(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
