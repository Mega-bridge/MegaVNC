package kr.co.megabridge.megavnc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class RegisterRemotePcDto {

    @NotBlank(message = "Pc의 이름을 입력해 주세요.")
    private String accessPassword;
    @NotBlank(message = "접근 비밀번호를 입력해 주세요.")
    private String remotePcName;

    private String groupName;
}
