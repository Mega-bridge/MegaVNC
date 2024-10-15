package kr.co.megabridge.megavnc.repository;

import kr.co.megabridge.megavnc.domain.Group;
import kr.co.megabridge.megavnc.domain.RemotePc;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RemotePcRepository extends CrudRepository<RemotePc, Long> {
    List<RemotePc> findByGroup(Group group);
    Optional<RemotePc> findTopByOrderByRepeaterIdDesc();
    Optional<RemotePc> findByRepeaterId(Long repeaterId);
    Optional<RemotePc> findByReconnectId(String reconnectId);
    List<RemotePc> findByName(String pcName);
    boolean existsByNameAndGroup(String pcName,Group group);
    void deleteAllByGroup(Group group);
}
