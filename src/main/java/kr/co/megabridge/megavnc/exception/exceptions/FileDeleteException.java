package kr.co.megabridge.megavnc.exception.exceptions;

import kr.co.megabridge.megavnc.exception.BusinessException;
import kr.co.megabridge.megavnc.exception.ErrorCode;

public class FileDeleteException  extends BusinessException {
    public FileDeleteException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public FileDeleteException(ErrorCode errorCode) {
        super(errorCode);
    }
}
