package kr.co.megabridge.megavnc;

import kr.co.megabridge.megavnc.domain.HostPC;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.repository.HostPcRepository;
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
            HostPcRepository hostPcRepository,
            UserRepository userRepository,
            PasswordEncoder encoder
    ) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                hostPcRepository.save(new HostPC("WinTestHost", "192.168.0.32", "5700"));
                hostPcRepository.save(new HostPC("PC1", "11.11.11.11", "5700"));
                hostPcRepository.save(new HostPC("PC2", "22.22.22.22", "5700"));
                hostPcRepository.save(new HostPC("PC3", "33.33.33.33", "5700"));
                hostPcRepository.save(new HostPC("PC4", "44.44.44.44", "5700"));

                User admin = User.createUser("admin", "1234", Set.of("ADMIN"), encoder);
                userRepository.save(admin);
            }
        };
    }

}
