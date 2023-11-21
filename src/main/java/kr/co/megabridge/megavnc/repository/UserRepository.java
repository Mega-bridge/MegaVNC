package kr.co.megabridge.megavnc.repository;

import kr.co.megabridge.megavnc.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
