package kr.co.megabridge.megavnc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminRemotePcRegisterDto {

    @NotBlank
    private String name;
}
