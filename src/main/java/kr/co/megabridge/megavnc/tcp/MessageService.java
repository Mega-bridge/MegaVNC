package kr.co.megabridge.megavnc.tcp;

import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.enums.Status;
import kr.co.megabridge.megavnc.exception.ErrorCode;
import kr.co.megabridge.megavnc.exception.exceptions.RemotePcException;
import kr.co.megabridge.megavnc.logManagement.LogService;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import kr.co.megabridge.megavnc.service.RemotePcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class MessageService {

    private final RemotePcService remotePcService;
    //예외가 호출된 클래스, 매서드, 라인 정보 반환
    private final LogService logService;
    private final RemotePcRepository remotePcRepository;




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
        RemotePc remotePc = remotePcRepository.findByRepeaterId(eventMessage.getRepeaterId()).orElseThrow(() -> new RemotePcException(ErrorCode.PC_NOT_FOUND));
        switch (eventMessage.getEvNum()) {
            case 2: // SERVER_CONNECT
                remotePcService.setRemotePcStatus(remotePc, Status.STANDBY);
                logService.saveAccessLog(remotePc,Status.STANDBY.getValue(),eventMessage.getIp());
                log.info("SERVER_CONNECT: " + eventMessage.getRepeaterId());
                break;
            case 3: // SERVER_DISCONNECT
                remotePcService.setRemotePcStatus(remotePc, Status.OFFLINE);
                logService.saveAccessLog(remotePc,Status.OFFLINE.getValue(),eventMessage.getIp());
                log.info("SERVER_DISCONNECT: " + eventMessage.getRepeaterId());
                break;
            case 4: // VIEWER_SERVER_SESSION_START
                remotePcService.setRemotePcStatus(remotePc, Status.ACTIVE);
                logService.saveAccessLog(remotePc,Status.ACTIVE.getValue(),eventMessage.getSvrIp());
                log.info("VIEWER_SERVER_SESSION_START: " + eventMessage.getRepeaterId());
                break;
            case 5: // VIEWER_SERVER_SESSION_END
                //api를 통해 먼저 연결 해제를 시켜버리므로 모든 값이 null, 즉 여기서 assinedAt이 null 이면 Offline 으로 하면 됨
                if(remotePc.getAssignedAt() == null){
                    remotePcService.setRemotePcStatus(remotePc, Status.OFFLINE);
                    logService.saveAccessLog(remotePc,Status.STANDBY.getValue(),eventMessage.getIp());
                }
                else {
                    remotePcService.setRemotePcStatus(remotePc, Status.STANDBY);
                    logService.saveAccessLog(remotePc,Status.STANDBY.getValue(),eventMessage.getIp());

                }
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
