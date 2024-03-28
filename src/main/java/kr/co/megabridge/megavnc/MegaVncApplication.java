package kr.co.megabridge.megavnc;

import kr.co.megabridge.megavnc.domain.*;
import kr.co.megabridge.megavnc.enums.Role;
import kr.co.megabridge.megavnc.repository.GroupRepository;
import kr.co.megabridge.megavnc.repository.Member_GroupRepository;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import kr.co.megabridge.megavnc.repository.MemberRepository;
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
            PasswordEncoder encoder,
            Member_GroupRepository member_groupRepository
    ) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {

                Optional<Group> findGroup = groupRepository.findById(1L);
                if (findGroup.isEmpty()) {
                    Group group = new Group(1L,"메가브릿지");
                    groupRepository.save(group);
                }

                Optional<Member> findAdmin = memberRepository.findByUsername("admin");
                if (findAdmin.isEmpty()) {
                    Group group = groupRepository.findById(1L).get();
                    User user = User.createUser("admin", "1234", Set.of( Role.toValue(Role.ROLE_ADMIN)), encoder);
                    Member admin = Member.createMember("admin", "1234", Role.toValue(Role.ROLE_ADMIN), encoder,user);
                    Member_Group member_group = new Member_Group(1L,admin,group);
                    memberRepository.save(admin);
                    member_groupRepository.save(member_group);
                }

                Optional<Member> findUser = memberRepository.findByUsername("user");
                if (findUser.isEmpty()) {
                    Group group = groupRepository.findById(1L).get();
                    User user = User.createUser("user", "1234", Set.of( Role.toValue(Role.ROLE_USER)), encoder);
                    Member member = Member.createMember("user", "1234", Role.toValue(Role.ROLE_USER), encoder, user);
                    Member_Group member_group = new Member_Group(2L,member,group);
                    memberRepository.save(member);
                    member_groupRepository.save(member_group);
                }

                // Repeater에서 99번 이하 ID 사용시, 자동으로 59xx번 ID로 변환되어서 100부터 시작
                Optional<RemotePc> findBase = remotePcRepository.findByRepeaterId(100L);
                if (findBase.isEmpty()) {
                    Group group = groupRepository.findById(1L).get();
                    RemotePc base = RemotePc.createRemotePc(100L, "BASE", "1234",null);
                    remotePcRepository.save(base);
                }


            }
        };
    }

}
