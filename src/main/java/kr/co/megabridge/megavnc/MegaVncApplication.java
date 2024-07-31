package kr.co.megabridge.megavnc;

import kr.co.megabridge.megavnc.domain.*;
import kr.co.megabridge.megavnc.enums.Role;
import kr.co.megabridge.megavnc.repository.GroupRepository;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import kr.co.megabridge.megavnc.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;


@SpringBootApplication
@EnableScheduling
public class MegaVncApplication {

    public static void main(String[] args) {
        SpringApplication.run(MegaVncApplication.class, args);
    }

    @Bean
    public CommandLineRunner dataLoader(
            RemotePcRepository remotePcRepository,
            MemberRepository memberRepository,
            GroupRepository groupRepository,
            PasswordEncoder encoder
    ) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {

                Optional<Group> findGroup = groupRepository.findById(1L);

                if (findGroup.isEmpty() ) {
                    Group group = Group.createGroup("BaseGroup");
                    groupRepository.save(group);
                }

                Optional<Member> findAdmin = memberRepository.findByUsername("admin");
                if (findAdmin.isEmpty()) {

                    Member admin = Member.createMember( "admin","!mb220719",Role.ROLE_ADMIN,encoder);
                    memberRepository.save(admin);

                }

                // Repeater에서 99번 이하 ID 사용시, 자동으로 59xx번 ID로 변환되어서 100부터 시작
                Optional<RemotePc> findBase = remotePcRepository.findByRepeaterId(100L);
                if (findBase.isEmpty()) {
                    Group group = groupRepository.findById(1L).get();
                    RemotePc base = RemotePc.createRemotePc(100L, "BASE", "1234",group);
                    remotePcRepository.save(base);
                }


            }
        };
    }

}
