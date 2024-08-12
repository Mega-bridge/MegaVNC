package kr.co.megabridge.megavnc.web.admin;


import jakarta.transaction.Transactional;
import kr.co.megabridge.megavnc.domain.Group;
import kr.co.megabridge.megavnc.domain.Member;
import kr.co.megabridge.megavnc.domain.Member_Group;
import kr.co.megabridge.megavnc.dto.requests.AssignGroupDto;
import kr.co.megabridge.megavnc.exception.ErrorCode;
import kr.co.megabridge.megavnc.exception.exceptions.AdminAssignException;
import kr.co.megabridge.megavnc.repository.GroupRepository;
import kr.co.megabridge.megavnc.repository.MemberRepository;
import kr.co.megabridge.megavnc.repository.Member_GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAssignService {

    private final GroupRepository groupRepository;
    private final Member_GroupRepository member_groupRepository;
    private final MemberRepository memberRepository;

    public Member findByUserId(Long userId){
        Optional<Member> optionalMember = memberRepository.findById(userId);
        return optionalMember.orElseThrow(()->new AdminAssignException(ErrorCode.USER_NOT_FOUND,userId));
    }

    public List<Group> listAssignedGroups(Long userId){
        Optional<Member> optionalMember = memberRepository.findById(userId);
        Member member = optionalMember.orElseThrow(() -> new AdminAssignException(ErrorCode.USER_NOT_FOUND,userId));
        List<Member_Group> member_groups = member_groupRepository.findByMember(member);
        return member_groups.stream()
                .filter(group -> group.getId() != 1L) // 아이디가 1L이 아닌 그룹 필터링
                .map(Member_Group::getGroup) // Member_Group 객체를 Group 엔티티로 매핑
                .toList();
    }


    public List<Group> listUnassignedGroups(Long userId){
        Optional<Member> optionalMember = memberRepository.findById(userId);
        Member member = optionalMember.orElseThrow(()->new AdminAssignException(ErrorCode.USER_NOT_FOUND,userId));
        List<Member_Group> member_groups = member_groupRepository.findByMemberNotContaining(member);
        return new ArrayList<>(member_groups.stream()
                .collect(Collectors.toMap(
                        group -> group.getGroup().getId(), // 그룹 아이디를 키로 사용
                        Member_Group::getGroup, // 그룹 엔티티를 값으로 사용
                        (existing, replacement) -> existing // 중복 발생 시 기존 값을 유지
                ))
                .values());
    }



    @Transactional
    public void assignGroup(AssignGroupDto assignGroupDto){
        Optional<Member> optionalMember = memberRepository.findById(assignGroupDto.getSelectedUserId());
        Member member = optionalMember.orElseThrow(()->new AdminAssignException(ErrorCode.USER_NOT_FOUND,assignGroupDto.getSelectedUserId()));
        if(assignGroupDto.getSelectedGroupId() == null){
            throw new AdminAssignException(ErrorCode.GROUP_NOT_FOUND, "그룹이 선택되지 않았습니다.",assignGroupDto.getSelectedUserId());
        }
        Optional<Group> optionalGroup = groupRepository.findById(assignGroupDto.getSelectedGroupId());
        Group group = optionalGroup.orElseThrow(()->new AdminAssignException(ErrorCode.GROUP_NOT_FOUND,assignGroupDto.getSelectedUserId()));

        List<Member_Group> member_groups = member_groupRepository.findByMemberAndGroup(member, group);
        if(!member_groups.isEmpty()){
            throw new AdminAssignException(ErrorCode.ALREADY_ASSIGNED_GROUP,assignGroupDto.getSelectedUserId());
        }
        Member_Group member_group = Member_Group.assignGroup(member, group);
        member_groupRepository.save(member_group);
    }


    @Transactional
    public void disassignGroup(Long memberId,Long groupId){
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        Member member = optionalMember.orElseThrow(()->new AdminAssignException(ErrorCode.USER_NOT_FOUND,memberId));
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        Group group = optionalGroup.orElseThrow(()->new AdminAssignException(ErrorCode.GROUP_NOT_FOUND,memberId));
        if(group.getId() == 1L){
            throw new AdminAssignException(ErrorCode.CANNOT_DELETE_DEFAULT_GROUP,memberId);
        }
        member_groupRepository.deleteByMemberAndGroup(member,group);
    }
}
