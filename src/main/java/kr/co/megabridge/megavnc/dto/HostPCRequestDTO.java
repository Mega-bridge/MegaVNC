package kr.co.megabridge.megavnc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HostPCRequestDTO {

    private final String name;
    private final String host;
    private final String port;
}
