package kr.co.megabridge.megavnc.logManagement;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface AccessLogsRepository extends JpaRepository<AccessLogs,Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM AccessLogs l WHERE l.timestamp < :cutoff")
    void deleteLogsOlderThan(LocalDateTime cutoff);
}
