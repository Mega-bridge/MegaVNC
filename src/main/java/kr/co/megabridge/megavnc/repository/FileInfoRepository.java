package kr.co.megabridge.megavnc.repository;

import kr.co.megabridge.megavnc.domain.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, String> {

    List<FileInfo> findAllByReconnectIdOrderByCreatedAtDesc(String reconnectId);
}
