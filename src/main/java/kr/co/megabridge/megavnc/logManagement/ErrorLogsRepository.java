package kr.co.megabridge.megavnc.logManagement;


import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface ErrorLogsRepository extends JpaRepository<ErrorLogs,Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM ErrorLogs l WHERE l.timestamp < :cutoff")
    void deleteLogsOlderThan(LocalDateTime cutoff);
}
