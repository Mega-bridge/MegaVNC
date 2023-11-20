package kr.co.megabridge.megavnc.repository;

import kr.co.megabridge.megavnc.domain.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);
}
