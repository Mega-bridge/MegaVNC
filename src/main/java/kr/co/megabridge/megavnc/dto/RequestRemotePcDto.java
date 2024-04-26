package kr.co.megabridge.megavnc.dto;

import lombok.Data;

@Data
public class RequestRemotePcDto {

    private String groupName;
    private String pcName;
    private String accessPassword;
    private String ftpHost;
    private String reconnectId;

}
