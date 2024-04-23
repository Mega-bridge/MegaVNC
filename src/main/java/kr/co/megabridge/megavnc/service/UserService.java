package kr.co.megabridge.megavnc.service;

import jakarta.transaction.Transactional;
import kr.co.megabridge.megavnc.domain.Member;
import kr.co.megabridge.megavnc.security.User;
import kr.co.megabridge.megavnc.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;





    @Transactional
    public void changePassword(User user, String newPassword) {
        Optional<Member> optionalMember = memberRepository.findByUsername(user.getUsername());
        if(optionalMember.isEmpty()){
            throw new UsernameNotFoundException("Username '" + user.getUsername() + "' not found.");
        }
        Member member = optionalMember.get();
        member.changePassword(newPassword,passwordEncoder);
        memberRepository.save(member);
    }



}
