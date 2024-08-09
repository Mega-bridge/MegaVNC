package kr.co.megabridge.megavnc.logManagement;

import kr.co.megabridge.megavnc.domain.RemotePc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogService {
    private final ErrorLogsRepository errorLogsRepository;
    private final AccessLogsRepository accessLogsRepository;

    @Value("${log.retention.days}")
    private Long retentionDays;

    @Transactional
    public void saveErrorLog(String message, String className, int lineNumber) {
        ErrorLogs errorLogs = ErrorLogs.builder()

                .message(message)
                .className(className)
                .lineNumber(lineNumber)
                .timestamp(LocalDateTime.now())
                .build();
        errorLogsRepository.save(errorLogs);
    }
    @Transactional
    public void saveAccessLog(RemotePc remotePc,String division,String ip ) {
        AccessLogs accessLogs = AccessLogs.builder()
                .timestamp(LocalDateTime.now())
                .groupName(remotePc.getGroup().getGroupName())
                .pcName(remotePc.getName())
                .reconnectId(remotePc.getReconnectId())
                .division(division)
                .ip(ip)
                .build();
        accessLogsRepository.save(accessLogs);
    }



    public void deleteOldLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);  // 30일 이전 로그 삭제
        errorLogsRepository.deleteLogsOlderThan(cutoff);
        accessLogsRepository.deleteLogsOlderThan(cutoff);
        log.info("Deleting old logs older than {}", retentionDays);
    }
}
