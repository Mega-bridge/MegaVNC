package kr.co.megabridge.megavnc.enums;
import lombok.Getter;

@Getter
public enum ErrorCode {


    //Group
    GROUP_NOT_FOUND(404, "G001", "존재하지 않는 그룹입니다. "),
    GROUP_NOT_SELECTED(400, "G002", "그룹이 선택되지 않았습니다. "),
    OWN_GROUP_ONLY(403, "G003", "자신의 그룹만 조회 가능합니다. "),
    OWN_GROUP_ONLY2(403, "G004", "자신의 그룹만 삭제 가능합니다. "),




    //RemotePc
    PC_NOT_FOUND(404, "R001", "존재하지 않는 PC입니다. "),
    PC_NAME_DUPLICATION(400, "R002", "이미 존재하는 PC이름 입니다. "),
    PASSWORD_NOT_MATCH(403, "R003", "접근 비밀번호가 일치하지 않습니다. "),
    OFFLINE_STATUS(400,"R004","오프라인 상태입니다. "),
    DELETE_NOT_ONLY_WHEN_ACTIVE(400,"R005","PC가 사용중일 때는 삭제가 불가능 합니다. "),
    MISSING_PC_NAME(400,"R006","pc의 이름을 입력해 주세요. "),
    MISSING_ACCESS_PASSWORD(400,"R007","접근 비밀번호를 입력해 주세요. "),
    ALREADY_ASSIGNED_PC(400,"R008","이미 배정된 pc 입니다. 다른 pc를 선택해 주세요. ");


    private final String code;
    private final String message;
    private final int status;

    ErrorCode(final int status, final String code, final String message) {
        this.status = status;
        this.message = message;
        this.code = code;
    }


}