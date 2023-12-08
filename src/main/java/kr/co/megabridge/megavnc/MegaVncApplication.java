package kr.co.megabridge.megavnc;

import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import kr.co.megabridge.megavnc.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

@SpringBootApplication
public class MegaVncApplication {

    public static void main(String[] args) {
        SpringApplication.run(MegaVncApplication.class, args);
    }

    @Bean
    public CommandLineRunner dataLoader(
            RemotePcRepository remotePcRepository,
            UserRepository userRepository,
            PasswordEncoder encoder
    ) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                Optional<User> findAdmin = userRepository.findByUsername("admin");
                if (findAdmin.isEmpty()) {
                    User admin = User.createUser("admin", "1234", Set.of("ROLE_ADMIN"), encoder);
                    userRepository.save(admin);
                }

                Optional<User> findUser = userRepository.findByUsername("user");
                if (findUser.isEmpty()) {
                    User user = User.createUser("user", "1234", Set.of("ROLE_USER"), encoder);
                    userRepository.save(user);
                }

                // Repeater에서 99번 이하 ID 사용시, 자동으로 59xx번 ID로 변환되어서 100부터 시작
                Optional<RemotePc> findBase = remotePcRepository.findByRepeaterId(100L);
                if (findBase.isEmpty()) {
                    User admin = userRepository.findByUsername("admin").orElseThrow();
                    RemotePc base = RemotePc.createRemotePc(100L, "BASE", admin);
                    remotePcRepository.save(base);
                }


            }
        };
    }

}
