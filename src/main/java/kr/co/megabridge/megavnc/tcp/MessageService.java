package kr.co.megabridge.megavnc.tcp;

import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.enums.Status;
import kr.co.megabridge.megavnc.service.RemotePcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageService {

    private final RemotePcService remotePcService;



    public void processMessage(byte[] message) {
        String msg = new String(message);
        //메시지 생성
        String[] tokens = msg.split(",");
        Map<String, String> msgKv = new HashMap<>();

        for (String token : tokens) {
            String[] kv = token.split(":");
            msgKv.put(kv[0], kv[1]);
        }

        EventMessage eventMessage = new EventMessage();
        switch (Integer.parseInt(msgKv.get("EvNum"))) {
            case 0, 1, 2, 3:
                eventMessage.setEvMsgVer(Integer.parseInt(msgKv.get("EvMsgVer")));
                eventMessage.setEvNum(Integer.parseInt(msgKv.get("EvNum")));
                eventMessage.setTime(Integer.parseInt(msgKv.get("Time")));
                eventMessage.setPid(Integer.parseInt(msgKv.get("Pid")));
                eventMessage.setTblInd(Integer.parseInt(msgKv.get("TblInd")));
                eventMessage.setCode(Long.parseLong(msgKv.get("Code")));
                eventMessage.setMode(Integer.parseInt(msgKv.get("Mode")));
                eventMessage.setIp(msgKv.get("Ip"));
                break;
            case 4, 5:
                eventMessage.setEvMsgVer(Integer.parseInt(msgKv.get("EvMsgVer")));
                eventMessage.setEvNum(Integer.parseInt(msgKv.get("EvNum")));
                eventMessage.setTime(Integer.parseInt(msgKv.get("Time")));
                eventMessage.setPid(Integer.parseInt(msgKv.get("Pid")));
                eventMessage.setSvrTblInd(Integer.parseInt(msgKv.get("SvrTblInd")));
                eventMessage.setVwrTblInd(Integer.parseInt(msgKv.get("VwrTblInd")));
                eventMessage.setCode(Long.parseLong(msgKv.get("Code")));
                eventMessage.setMode(Integer.parseInt(msgKv.get("Mode")));
                eventMessage.setSvrIp(msgKv.get("SvrIp"));
                eventMessage.setVwrIp(msgKv.get("VwrIp"));
                break;
            case 6, 7, 8:
                eventMessage.setEvMsgVer(Integer.parseInt(msgKv.get("EvMsgVer")));
                eventMessage.setEvNum(Integer.parseInt(msgKv.get("EvNum")));
                eventMessage.setTime(Integer.parseInt(msgKv.get("Time")));
                eventMessage.setPid(Integer.parseInt(msgKv.get("Pid")));
                eventMessage.setMaxSessions(Integer.parseInt(msgKv.get("MaxSessions")));
                break;
            default:
                throw new RuntimeException("Message parse exception");
        }

        switch (eventMessage.getEvNum()) {
            case 2: // SERVER_CONNECT
                remotePcService.setRemotePcStatus(eventMessage.getRepeaterId(), Status.STANDBY);
                log.info("SERVER_CONNECT: " + eventMessage.getRepeaterId());
                break;
            case 3: // SERVER_DISCONNECT
                remotePcService.setRemotePcStatus(eventMessage.getRepeaterId(), Status.OFFLINE);
                log.info("SERVER_DISCONNECT: " + eventMessage.getRepeaterId());
                break;
            case 4: // VIEWER_SERVER_SESSION_START
                remotePcService.setRemotePcStatus(eventMessage.getRepeaterId(), Status.ACTIVE);
                log.info("VIEWER_SERVER_SESSION_START: " + eventMessage.getRepeaterId());
                break;
            case 5: // VIEWER_SERVER_SESSION_END
                remotePcService.setRemotePcStatus(eventMessage.getRepeaterId(), Status.STANDBY);
                log.info("VIEWER_SERVER_SESSION_END: " + eventMessage.getRepeaterId());
                break;
            case 6: // REPEATER_STARTUP // debug // FIXME
                log.info("REPEATER_STARTUP");
                break;
            case 7: // REPEATER_SHUTDOWN // debug // FIXME
                log.info("REPEATER_SHUTDOWN");
                break;
            default: // no-op
                break;
        }


    }
}
