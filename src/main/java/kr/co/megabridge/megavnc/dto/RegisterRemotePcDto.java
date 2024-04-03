package kr.co.megabridge.megavnc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class RegisterRemotePcDto {


    private String accessPassword;

    private String remotePcName;

    private String groupName;
}
