package kr.co.megabridge.megavnc;

import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import kr.co.megabridge.megavnc.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

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
                User admin = User.createUser("admin", "1234", Set.of("ADMIN"), encoder);
                userRepository.save(admin);

                remotePcRepository.save(RemotePc.createRemotePc("123456789", "WinTestHost", admin));
            }
        };
    }

}
