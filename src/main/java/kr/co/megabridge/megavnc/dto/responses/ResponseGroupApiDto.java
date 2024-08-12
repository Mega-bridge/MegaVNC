package kr.co.megabridge.megavnc.dto.responses;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseGroupApiDto {
    private Long groupId;
    private String groupName;
}
