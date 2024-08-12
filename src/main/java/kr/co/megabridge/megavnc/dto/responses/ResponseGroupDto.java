package kr.co.megabridge.megavnc.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseGroupDto {
    private Long groupId;
    private String groupName;
}
