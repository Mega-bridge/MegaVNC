package kr.co.megabridge.megavnc.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestRemotePcDto {

    @NotBlank(message = "그룹을 선택해 주세요")
    private String groupName;

    @NotBlank(message = "PC 이름을 입력해 주세요.")
    @Size(min = 2, max = 16, message = "길이 2 ~ 16 글자로 입력해 주세요.")
    private String pcName;

    @NotBlank(message = "접근 비밀번호를 입력해 주세요.")
    private String accessPassword;

    private String reconnectId;

}
