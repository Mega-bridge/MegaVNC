package kr.co.megabridge.megavnc.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AssignGroupDto {


    private Long selectedGroupId;
    private Long selectedUserId;
}
