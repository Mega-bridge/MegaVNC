package kr.co.megabridge.megavnc.exception.exceptions;

import kr.co.megabridge.megavnc.exception.ErrorCode;
import kr.co.megabridge.megavnc.exception.BusinessException;

public class AdminGroupException extends BusinessException {
    public AdminGroupException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public AdminGroupException(ErrorCode errorCode) {
        super(errorCode);
    }
}
