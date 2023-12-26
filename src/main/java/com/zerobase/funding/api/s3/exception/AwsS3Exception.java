package com.zerobase.funding.api.s3.exception;

import com.zerobase.funding.api.exception.CustomException;
import com.zerobase.funding.api.exception.ErrorCode;

public class AwsS3Exception extends CustomException {


    public AwsS3Exception(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
