package kr.co.megabridge.megavnc.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupRegisterDto {
    @NotBlank(message = "그룹 이름을 입력해 주세요")
    @Size(min = 2, max = 16, message = "길이 2 ~ 16 글자로 입력해 주세요.")
    private String groupName;
}
