package kr.co.megabridge.megavnc.dto;

import jakarta.validation.constraints.NotBlank;
import kr.co.megabridge.megavnc.domain.HostPC;
import lombok.Data;

@Data
public class HostPcRequestDto {

    @NotBlank
    private String name;

    @NotBlank
    private String host;

    @NotBlank
    private String port;

    public HostPC toEntity() {
        return new HostPC(name, host, port);
    }
}
