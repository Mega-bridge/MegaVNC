package kr.co.megabridge.megavnc.exception;
import lombok.Getter;

@Getter
public enum ErrorCode {


    //Group
    GROUP_NOT_FOUND(404, "G001", "존재하지 않는 그룹입니다. "),
    GROUP_NOT_SELECTED(400, "G002", "그룹이 선택되지 않았습니다. "),
    OWN_GROUP_ONLY(403, "G003", "자신의 그룹만 조회 가능합니다. "),
    OWN_GROUP_ONLY2(403, "G004", "자신의 그룹만 삭제 가능합니다. "),
    ALREADY_EXIST_GROUP(400, "G005", "이미 존재하는 그룹입니다. "),
    CANNOT_DELETE_DEFAULT_GROUP(400, "G006", "기본 그룹은 삭제할 수 없습니다. "),
    ALREADY_ASSIGNED_GROUP(400,"G007","이미 배정된 그룹입니다. "),


    //RemotePc
    PC_NOT_FOUND(404, "R001", "존재하지 않는 PC입니다. "),
    PC_NAME_DUPLICATION(400, "R002", "이미 존재하는 PC이름 입니다. "),
    PASSWORD_NOT_MATCH(403, "R003", "접근 비밀번호가 일치하지 않습니다. "),
    OFFLINE_STATUS(400,"R004","오프라인 상태입니다. "),
    DELETE_NOT_ONLY_WHEN_ACTIVE(400,"R005","PC가 사용중일 때는 삭제가 불가능 합니다. "),
    MISSING_PC_NAME(400,"R006","pc의 이름을 입력해 주세요. "),
    MISSING_ACCESS_PASSWORD(400,"R007","접근 비밀번호를 입력해 주세요. "),
    CANNOT_DELETE_DEFAULT_PC(400,"R008","기본 pc는 삭제할 수 없습니다. "),
    ALREADY_ASSIGNED_PC(400,"R009","이미 등록된 pc 입니다. 다른 pc를 선택해 주세요. "),
    CANNOT_DELETE_ASSIGNED_PC(400,"R010","등록되지 않은 pc만 삭제할 수 있습니다. "),
    CANNOT_DISASSIGNED_PC(400,"R011","이미 연결이 해제된 pc입니다."),



    //User
    USER_NOT_FOUND(404,"U001","존재하지 않는 사용자 입니다. "),
    ADMIN_CANNOT_DELETE(400,"U002","어드민은 삭제할 수 없습니다. "),

    //File
    FILE_NOT_FOUND(404,"F001","파일 다운로드 실패 : 파일을 서버에서 찾을 수 없음 "),
    FILE_CANNOT_UPLOAD(400,"F002","파일 업로드 실패 : 파일을 업로드 할 수 없음 "),
    ENCODING_FAIL(400,"F003","파일 업로드 실패 : 파일명을 해시 할 수 없음"),
    FILE_CANNOT_DELETE(400,"F004","파일 다운로드 실패 : 파일을 서버에서 삭제하지 못하였습니다. ");

    private final String code;
    private final String message;
    private final int status;

    ErrorCode(final int status, final String code, final String message) {
        this.status = status;
        this.message = message;
        this.code = code;
    }


}