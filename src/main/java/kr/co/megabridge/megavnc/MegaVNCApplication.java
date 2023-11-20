package kr.co.megabridge.megavnc;

import kr.co.megabridge.megavnc.domain.HostPC;
import kr.co.megabridge.megavnc.repository.HostPCRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MegaVNCApplication {

    public static void main(String[] args) {
        SpringApplication.run(MegaVNCApplication.class, args);
    }

    @Bean
    public CommandLineRunner dataLoader(HostPCRepository repo) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                repo.save(new HostPC("WinTestHost", "192.168.0.32", "5700"));
                repo.save(new HostPC("PC1", "11.11.11.11", "5700"));
                repo.save(new HostPC("PC2", "22.22.22.22", "5700"));
                repo.save(new HostPC("PC3", "33.33.33.33", "5700"));
                repo.save(new HostPC("PC4", "44.44.44.44", "5700"));
            }
        };
    }

}
