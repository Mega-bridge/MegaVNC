package kr.co.megabridge.megavnc.service;

import kr.co.megabridge.megavnc.domain.Member;
import kr.co.megabridge.megavnc.enums.Role;
import kr.co.megabridge.megavnc.security.JwtTokenProvider;
import kr.co.megabridge.megavnc.domain.JwtToken;
import kr.co.megabridge.megavnc.security.User;
import kr.co.megabridge.megavnc.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserService(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManagerBuilder authenticationManagerBuilder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void register(String username, String password) {
        User user = User.createUser(username, password, Set.of(Role.toValue(Role.ROLE_USER)), passwordEncoder);
        Member member = Member.createMember(username,password,Role.toValue(Role.ROLE_USER),passwordEncoder,user);
        memberRepository.save(member);
    }

    public boolean isUsernameUnique(String username) {
        Optional<Member> member = memberRepository.findByUsername(username);
        return member.isEmpty();
    }

    public List<Member> listAllUsers() {
        return memberRepository.findAll();
    }

    public void changePassword(User user, String newPassword) {
        Optional<Member> optionalMember = memberRepository.findByUsername(user.getUsername());
        if(optionalMember.isEmpty()){
            throw new UsernameNotFoundException("Username '" + user.getUsername() + "' not found.");
        }
        Member member = optionalMember.get();
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    public JwtToken getJwtToken(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        return jwtTokenProvider.generateToken(authentication);
    }

    public Optional<Member> authUser(String username, String rawPassword) {
        Optional<Member> member = memberRepository.findByUsername(username);

        if (member.isEmpty())
            return Optional.empty();

        if (!passwordEncoder.matches(rawPassword, member.get().getPassword()))
            return Optional.empty();

        return member;
    }

}
