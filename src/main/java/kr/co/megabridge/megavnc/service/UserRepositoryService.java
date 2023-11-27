package kr.co.megabridge.megavnc.service;

import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserRepositoryService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserRepositoryService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Long register(String username, String password) {
        User user = User.createUser(username, password, Set.of("USER"), passwordEncoder);
        userRepository.save(user);

        return user.getId();
    }

    public boolean isUsernameUnique(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.isEmpty();
    }

    public List<User> listAllUsers() {
        return userRepository.findAll();
    }
}
