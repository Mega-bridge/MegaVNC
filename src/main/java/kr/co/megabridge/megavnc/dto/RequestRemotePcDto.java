package kr.co.megabridge.megavnc.dto;

import lombok.Data;

@Data
public class RequestRemotePcDto {

    private String pcName;
    private String accessPassword;

}
