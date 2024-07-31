package kr.co.megabridge.megavnc.repository;

import kr.co.megabridge.megavnc.domain.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, String> {

    List<FileInfo> findAllByReconnectIdOrderByCreatedAtDesc(String reconnectId);
    Optional<FileInfo> findBySeq(Integer seq);
    void deleteBySeq(Integer seq);
    List<FileInfo> findAllByCreatedAtBeforeAndReconnectIdIsNot(Date date,String reconnectId);
}
