package kr.co.megabridge.megavnc.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class RegisterRemotePcDto {

    @NotBlank(message = "접근 비밀번호를 입력해 주세요.")
    private String accessPassword;

    @NotBlank(message = "PC 이름을 입력해 주세요.")
    private String remotePcName;

    @NotBlank(message = "그룹을 선택해 주세요")
    private String groupName;
}
