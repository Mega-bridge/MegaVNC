package kr.co.megabridge.megavnc.dto;

import lombok.Data;

@Data
public class RemotePcRegisterApiDto {

    private String username;
    private String password;
    private String remotePcName;
}
