package kr.co.megabridge.megavnc.dto;

import jakarta.validation.constraints.NotBlank;
import kr.co.megabridge.megavnc.domain.RemotePc;
import lombok.Data;

@Data
public class RemotePcRegisterDto {

    @NotBlank
    private String name;
}
