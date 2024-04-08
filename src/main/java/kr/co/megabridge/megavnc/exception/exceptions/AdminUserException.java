package kr.co.megabridge.megavnc.exception.exceptions;

import kr.co.megabridge.megavnc.exception.ErrorCode;
import kr.co.megabridge.megavnc.exception.BusinessException;

public class AdminUserException extends BusinessException {
    public AdminUserException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public AdminUserException(ErrorCode errorCode) {
        super(errorCode);
    }
}
