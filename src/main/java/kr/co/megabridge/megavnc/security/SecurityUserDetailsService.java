package kr.co.megabridge.megavnc.security;

import kr.co.megabridge.megavnc.domain.Member;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;


@Service
public class SecurityUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Autowired
    public SecurityUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Member> optionalMember = memberRepository.findByUsername(username);
        if (!optionalMember.isPresent()){
            throw new UsernameNotFoundException("Username '" + username + "' not found.");
        }
        Member member = optionalMember.get();
        User user = member.getUserDetail();
        return user;
    }


}
