package kr.co.megabridge.megavnc.exception.exceptions;

import kr.co.megabridge.megavnc.exception.ErrorCode;
import kr.co.megabridge.megavnc.exception.BusinessException;


public class RemotePcApiException extends BusinessException {

    public RemotePcApiException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public RemotePcApiException(ErrorCode errorCode) {
        super(errorCode);
    }
}
