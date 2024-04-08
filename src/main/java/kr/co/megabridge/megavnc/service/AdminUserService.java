package kr.co.megabridge.megavnc.service;

import jakarta.transaction.Transactional;
import kr.co.megabridge.megavnc.domain.Group;
import kr.co.megabridge.megavnc.domain.Member;
import kr.co.megabridge.megavnc.domain.Member_Group;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.dto.AssignGroupDto;
import kr.co.megabridge.megavnc.dto.ResponseGroupDto;
import kr.co.megabridge.megavnc.enums.Role;
import kr.co.megabridge.megavnc.exception.ErrorCode;
import kr.co.megabridge.megavnc.exception.exceptions.AdminUserException;
import kr.co.megabridge.megavnc.repository.GroupRepository;
import kr.co.megabridge.megavnc.repository.MemberRepository;
import kr.co.megabridge.megavnc.repository.Member_GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final Member_GroupRepository member_groupRepository;
    private final GroupRepository groupRepository;

    public List<Member> listAllUsers(){
        return memberRepository.findAllStartWith(2L);
    }

    @Transactional
    public void register(String username, String password) {

        //bindingResult로 처리해서 각각의 입력 필드 아래에 두기
        //Todo: 공백일 경우 처리 해야함
        //Todo: 같은 사용자 명의 사용자가 존재하는지 확인해야 함
        //Todo: 입력한 패스워드와 확인 패스워드가 일치하는지 확인해야 함
        User user = User.createUser(username, password, Set.of(Role.toValue(Role.ROLE_USER)), passwordEncoder);
        Member member = Member.createMember(username,Role.ROLE_USER,user);
        memberRepository.save(member);
    }


    public List<ResponseGroupDto> findAllGroups(){
        List<Group> groups = groupRepository.findAllStartWith(2L);
        List<ResponseGroupDto> responseGroupDtos = new ArrayList<>();
        for (Group group : groups){
            ResponseGroupDto responseGroupDto = new ResponseGroupDto(group.getId(), group.getGroupName());
            responseGroupDtos.add(responseGroupDto);
        }
        return responseGroupDtos;
    }



    @Transactional
    public void deleteUser(Long memberId){

        Optional<Member> optionalMember = memberRepository.findById(memberId);
        Member member  = optionalMember.orElseThrow(() -> new AdminUserException(ErrorCode.USER_NOT_FOUND));
        if(member.getRole() == Role.ROLE_ADMIN){
            throw new AdminUserException(ErrorCode.ADMIN_CANNOT_DELETE);
        }
        member_groupRepository.deleteAllByMember(member);
        memberRepository.delete(member);
    }



}
