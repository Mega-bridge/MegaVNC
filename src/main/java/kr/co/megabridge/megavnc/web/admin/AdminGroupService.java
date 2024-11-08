package kr.co.megabridge.megavnc.web.admin;

import jakarta.transaction.Transactional;
import kr.co.megabridge.megavnc.domain.Group;

import kr.co.megabridge.megavnc.domain.Member;
import kr.co.megabridge.megavnc.domain.Member_Group;
import kr.co.megabridge.megavnc.dto.responses.ResponseGroupApiDto;
import kr.co.megabridge.megavnc.enums.Role;
import kr.co.megabridge.megavnc.exception.ErrorCode;
import kr.co.megabridge.megavnc.exception.exceptions.AdminGroupException;
import kr.co.megabridge.megavnc.repository.GroupRepository;
import kr.co.megabridge.megavnc.repository.MemberRepository;
import kr.co.megabridge.megavnc.repository.Member_GroupRepository;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AdminGroupService {
  private final GroupRepository groupRepository;
  private final Member_GroupRepository member_groupRepository;
  private final MemberRepository memberRepository;
  private final RemotePcRepository remotePcRepository;


  public List<Group> listAllGroups(){

    return  groupRepository.findAllExceptIdOne();
  }


    @Transactional
    public void register(ResponseGroupApiDto responseGroupApiDto){
      String groupName = responseGroupApiDto.getGroupName();
      Optional<Group> optionalGroup = groupRepository.findByGroupName(groupName);
      if(optionalGroup.isPresent()){
        if (optionalGroup.get().getId() == 1L){
          throw new AdminGroupException(ErrorCode.ALREADY_EXIST_GROUP,"'BaseGroup'이라는 그룹명을 사용할 수 없습니다.");
        }
        throw new AdminGroupException(ErrorCode.ALREADY_EXIST_GROUP);
      }
      Group group = Group.createGroup(groupName);
      Member admin = memberRepository.findByRole(Role.ROLE_ADMIN).get();
      Member_Group member_group = Member_Group.assignGroup(admin,group);
      groupRepository.save(group);
      member_groupRepository.save(member_group);
    }

  @Transactional
  public void deleteGroup(Long groupId){
    Optional<Group> optionalGroup = groupRepository.findById(groupId);
    Group group  = optionalGroup.orElseThrow(() -> new AdminGroupException(ErrorCode.GROUP_NOT_FOUND));
    if(group.getId()==1L){
      throw new AdminGroupException(ErrorCode.CANNOT_DELETE_DEFAULT_GROUP);
    }
    remotePcRepository.deleteAllByGroup(group);
    member_groupRepository.deleteAllByGroup(group);
    groupRepository.delete(group);
  }
}
