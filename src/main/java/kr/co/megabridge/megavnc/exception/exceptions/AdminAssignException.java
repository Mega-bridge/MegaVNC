package kr.co.megabridge.megavnc.exception.exceptions;

import kr.co.megabridge.megavnc.exception.BusinessException;
import kr.co.megabridge.megavnc.exception.ErrorCode;
import lombok.Getter;

@Getter
public class AdminAssignException extends BusinessException {

    private final Long memberId;

    public AdminAssignException(ErrorCode errorCode, String message, Long memberId) {
        super(errorCode, message);
        this.memberId = memberId;
    }



    public AdminAssignException(ErrorCode errorCode, Long memberId) {
        super(errorCode);
        this.memberId = memberId;
    }
}
