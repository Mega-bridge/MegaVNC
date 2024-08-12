package kr.co.megabridge.megavnc.dto.requests;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AssignGroupDto {

    @NotBlank(message = "그룹을 선택해 주세요.")
    private Long selectedGroupId;

    private Long selectedUserId;
}
