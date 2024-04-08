package kr.co.megabridge.megavnc.tcp;

import lombok.Data;

@Data
public class EventMessage {

    private int evMsgVer;
    private int evNum;
    private long time;
    private int pid;
    private int tblInd;
    private long code;
    private int mode;
    private String ip;

    private int svrTblInd;
    private int vwrTblInd;
    private String svrIp;
    private String vwrIp;

    private int maxSessions;

    public Long getRepeaterId() {
        return code;
    }



}
