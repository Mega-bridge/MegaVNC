package kr.co.megabridge.megavnc.exception.exceptions;


import kr.co.megabridge.megavnc.exception.ErrorCode;
import kr.co.megabridge.megavnc.exception.BusinessException;


public  class RemotePcException extends BusinessException {

    public RemotePcException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public RemotePcException(ErrorCode errorCode) {
        super(errorCode);
    }
}
