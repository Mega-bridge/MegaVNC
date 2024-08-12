package kr.co.megabridge.megavnc.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRegisterDto {


    @NotBlank(message = "이름을 입력해 주세요.")
    private String username;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    private String password;

    @NotBlank(message = "확인 비밀번호를 입력해 주세요.")
    private String passwordConfirm;
}
