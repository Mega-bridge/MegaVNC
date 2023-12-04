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

    public String getRepeaterId() {
        return String.format("%09d", code);
    }


    public static enum EvNum {
        VIEWER_CONNECT,                 // 0
        VIEWER_DISCONNECT,              // 1
        SERVER_CONNECT,                 // 2 // TODO
        SERVER_DISCONNECT,              // 3 // TODO
        VIEWER_SERVER_SESSION_START,    // 4 // TODO
        VIEWER_SERVER_SESSION_END,      // 5 // TODO
        REPEATER_STARTUP,               // 6
        REPEATER_SHUTDOWN,              // 7
        REPEATER_HEARTBEAT              // 8
    }
}
