package kr.co.megabridge.megavnc;

import kr.co.megabridge.megavnc.domain.Member;
import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.domain.Segment;
import kr.co.megabridge.megavnc.enums.Role;
import kr.co.megabridge.megavnc.repository.GroupRepository;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import kr.co.megabridge.megavnc.repository.MemberRepository;
import kr.co.megabridge.megavnc.domain.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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
            MemberRepository memberRepository,
            GroupRepository groupRepository,
            PasswordEncoder encoder
    ) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {

                Optional<Segment> findGroup = groupRepository.findById(1L);
                if (findGroup.isEmpty()) {
                    Segment segment = new Segment(1L,"메가브릿지");
                    groupRepository.save(segment);
                }

                Optional<Member> findAdmin = memberRepository.findByUsername("admin");
                if (findAdmin.isEmpty()) {
                    Segment group = groupRepository.findBySegmentName("메가브릿지");
                    User user = User.createUser("admin", "1234", Set.of( Role.toValue(Role.ROLE_ADMIN)), encoder);
                    Member admin = Member.createMember("admin", "1234", Role.toValue(Role.ROLE_ADMIN), encoder,user, group);
                    memberRepository.save(admin);
                }

                Optional<Member> findUser = memberRepository.findByUsername("user");
                if (findUser.isEmpty()) {
                    Segment group = groupRepository.findBySegmentName("메가브릿지");
                    User user = User.createUser("user", "1234", Set.of( Role.toValue(Role.ROLE_USER)), encoder);
                    Member member = Member.createMember("user", "1234", Role.toValue(Role.ROLE_USER), encoder, user, group);
                    memberRepository.save(member);
                }

                // Repeater에서 99번 이하 ID 사용시, 자동으로 59xx번 ID로 변환되어서 100부터 시작
                Optional<RemotePc> findBase = remotePcRepository.findByRepeaterId(100L);
                if (findBase.isEmpty()) {
                    Member admin = memberRepository.findByUsername("admin").orElseThrow();
                    RemotePc base = RemotePc.createRemotePc(100L, "BASE", "1234",admin);
                    remotePcRepository.save(base);
                }


                // test pcs
                /*
                User user = userRepository.findByUsername("user").orElseThrow();
                for (int i = 0; i < 10; i++) {
                    RemotePc testPc = RemotePc.createRemotePc(i + 200L, "내 데스크탑 " + (i + 1), user);
                    remotePcRepository.save(testPc);
                }
                 */
            }
        };
    }

}
