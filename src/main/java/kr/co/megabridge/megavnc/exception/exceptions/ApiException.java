package kr.co.megabridge.megavnc.exception.exceptions;

import kr.co.megabridge.megavnc.exception.ErrorCode;
import kr.co.megabridge.megavnc.exception.BusinessException;


public class ApiException extends BusinessException{

    public ApiException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ApiException(ErrorCode errorCode) {
        super(errorCode);
    }
}

