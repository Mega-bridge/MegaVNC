package kr.co.megabridge.megavnc.exception.exceptions;

import kr.co.megabridge.megavnc.exception.BusinessException;
import kr.co.megabridge.megavnc.exception.ErrorCode;

public class FileDownloadException  extends BusinessException {
    public FileDownloadException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public FileDownloadException(ErrorCode errorCode) {
        super(errorCode);
    }
}
