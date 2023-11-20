package kr.co.megabridge.megavnc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class HostPCResponseDTO {

    private Long id;
    private Date createdAt;
    private String name;
    private String host;
    private String port;
}
