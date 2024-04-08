package kr.co.megabridge.megavnc.dto;

import kr.co.megabridge.megavnc.domain.Member;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AssignGroupDto {

    private Long selectedGroupId;
    private Long selectedUserId;
}
