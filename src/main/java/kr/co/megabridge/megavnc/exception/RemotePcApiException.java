package kr.co.megabridge.megavnc.exception;

import kr.co.megabridge.megavnc.enums.ErrorCode;
import lombok.Getter;

@Getter
public class RemotePcApiException extends RuntimeException{

    private final ErrorCode errorCode;

    public RemotePcApiException(ErrorCode errorCode, String message) {
        super(errorCode.getMessage() + message);
        this.errorCode = errorCode;
    }


    public RemotePcApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
