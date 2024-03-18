package kr.co.megabridge.megavnc.repository;

import kr.co.megabridge.megavnc.domain.Member;
import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.security.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RemotePcRepository extends CrudRepository<RemotePc, Long> {
    Iterable<RemotePc> findByOwner(Member member);
    Optional<RemotePc> findTopByOrderByRepeaterIdDesc();
    Optional<RemotePc> findByRepeaterId(Long repeaterId);
}
