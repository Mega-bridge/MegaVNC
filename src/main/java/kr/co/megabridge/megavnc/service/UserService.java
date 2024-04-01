package kr.co.megabridge.megavnc.service;

import kr.co.megabridge.megavnc.domain.Member;
import kr.co.megabridge.megavnc.domain.Group;
import kr.co.megabridge.megavnc.dto.ResponseGroupDto;
import kr.co.megabridge.megavnc.enums.Role;
import kr.co.megabridge.megavnc.repository.GroupRepository;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final GroupRepository groupRepository;

    public void register(String username, String password) {
        User user = User.createUser(username, password, Set.of(Role.toValue(Role.ROLE_USER)), passwordEncoder);
        Member member = Member.createMember(username,Role.toValue(Role.ROLE_USER),user);
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
        member.getUserDetail().setPassword(passwordEncoder.encode(newPassword));
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



}
