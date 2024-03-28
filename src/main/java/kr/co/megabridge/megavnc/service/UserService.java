package kr.co.megabridge.megavnc.service;

import kr.co.megabridge.megavnc.domain.Member;
import kr.co.megabridge.megavnc.domain.Group;
import kr.co.megabridge.megavnc.dto.ResponseGroupDto;
import kr.co.megabridge.megavnc.enums.Role;
import kr.co.megabridge.megavnc.repository.GroupRepository;
import kr.co.megabridge.megavnc.security.JwtTokenProvider;
import kr.co.megabridge.megavnc.domain.JwtToken;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final GroupRepository groupRepository;

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
        //FIXME :세터 사용 금지
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }
    public List<ResponseGroupDto> findAllGroup(){
        List<Group> groups = groupRepository.findAll();
        List<ResponseGroupDto> ResponseGroupDtos = new ArrayList<>();
        for (Group group : groups){
            ResponseGroupDto responseGroupDto = new ResponseGroupDto(group.getId(), group.getGroupName());
            ResponseGroupDtos.add(responseGroupDto);
        }
        return ResponseGroupDtos;
    }

    public JwtToken getJwtToken(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        return jwtTokenProvider.generateToken(authentication);
    }

    public Optional<Member> authUser(String username, String password) {
        Optional<Member> member = memberRepository.findByUsername(username);

        if (member.isEmpty())
            return Optional.empty();
        if(passwordEncoder.matches(password,member.get().getUserDetail().getPassword())){
            System.out.println("member.get().getPassword() = " + member.get().getUserDetail().getPassword());
            System.out.println("password = " + password);
            throw new RuntimeException("비밀번호가 일치하지 않습니다");}
        return member;
    }

}
