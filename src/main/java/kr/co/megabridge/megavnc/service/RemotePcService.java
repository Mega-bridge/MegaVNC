package kr.co.megabridge.megavnc.service;

import jakarta.transaction.Transactional;
import kr.co.megabridge.megavnc.domain.*;
import kr.co.megabridge.megavnc.dto.*;
import kr.co.megabridge.megavnc.enums.ErrorCode;
import kr.co.megabridge.megavnc.enums.Status;
import kr.co.megabridge.megavnc.exception.RemotePcException;
import kr.co.megabridge.megavnc.repository.GroupRepository;
import kr.co.megabridge.megavnc.repository.MemberRepository;
import kr.co.megabridge.megavnc.repository.Member_GroupRepository;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;



@Service
public class RemotePcService {
    private static final String defaultGroup = "All Group";

    private final RemotePcRepository remotePcRepository;
    private final MemberRepository memberRepository;
    private final Member_GroupRepository member_groupRepository;
    private final GroupRepository groupRepository;

    @Autowired
    public RemotePcService(RemotePcRepository remotePcRepository,
                           MemberRepository memberRepository,
                           Member_GroupRepository member_groupRepository,
                           GroupRepository groupRepository) {
        this.remotePcRepository = remotePcRepository;
        this.memberRepository = memberRepository;
        this.member_groupRepository = member_groupRepository;
        this.groupRepository = groupRepository;
    }

    public Iterable<RemotePc> findAll() {
        return remotePcRepository.findAll();
    }


    public List<ResponseRemotePcDto> findByGroups(List<Group> groups) {
        List<ResponseRemotePcDto> responses = new ArrayList<>();
        for(Group group : groups){
            List<RemotePc> remotePcs = remotePcRepository.findByGroup(group);
                for (RemotePc remotePc :remotePcs){
                   ResponseRemotePcDto response = new ResponseRemotePcDto(remotePc.getId(),remotePc.getGroup(),remotePc.getName(),remotePc.getCreatedAt(),remotePc.getStatus());
                    responses.add(response);
            }
        }
        Collections.sort(responses, Comparator.comparing(ResponseRemotePcDto::getId));
        return responses;
    }

    public List<ResponseRemotePcDto> findByGroupName(String groupName ,User user){
        Optional<Group> optionalGroup = groupRepository.findByGroupName(groupName);
        List<ResponseRemotePcDto> responses = new ArrayList<>();
        Group group = optionalGroup.orElseThrow(() -> new RemotePcException(ErrorCode.GROUP_NOT_FOUND,"해당 그룹을 추가해 주세요."));
        Optional<Member> optionalMember = memberRepository.findByUsername(user.getUsername());
        Member member = optionalMember.orElseThrow(() -> new UsernameNotFoundException("코드를 수정하여 존재하지 않는 멤버에 대한 접근을 막으세요"));


        List<Member_Group> myGroups = member_groupRepository.findByMemberAndGroup(member,group);
        if(myGroups.isEmpty()){
            throw new RemotePcException(ErrorCode.OWN_GROUP_ONLY);
        }

        List<RemotePc> remotePcs = remotePcRepository.findByGroup(group);
        for (RemotePc remotePc :remotePcs){
            ResponseRemotePcDto response = new ResponseRemotePcDto(remotePc.getId(),remotePc.getGroup(),remotePc.getName(),remotePc.getCreatedAt(),remotePc.getStatus());
            responses.add(response);
        }
        Collections.sort(responses, Comparator.comparing(ResponseRemotePcDto::getId));
        return responses;
    }

    public List<Group> findGroupByMember(User user){
        Optional<Member> optionalMember = memberRepository.findByUsername(user.getUsername());

        Member member = optionalMember.orElseThrow(() -> new UsernameNotFoundException("코드를 수정하여 존재하지 않는 멤버에 대한 접근을 막으세요"));
        List<Member_Group> myGroups = member_groupRepository.findByMember(member);
        List<Group> groups = new ArrayList<>();
        for(Member_Group member_group : myGroups){
            Group group = member_group.getGroup();
            groups.add(group);
        }

        return groups;
    }

    @Transactional
    public void registerRemotePc(RegisterRemotePcDto registerRemotePcDto) {
        Optional<RemotePc> currTop = remotePcRepository.findTopByOrderByRepeaterIdDesc();

        // Make sure the repeater id starts from 100.
        // Base remote pc in data loader also ensures it.
        long nextRepeaterId = 100;
        if (registerRemotePcDto.getRemotePcName().isEmpty()){
            throw new RemotePcException(ErrorCode.MISSING_PC_NAME);
        }
        if (registerRemotePcDto.getAccessPassword().isEmpty()){
            throw new RemotePcException(ErrorCode.MISSING_ACCESS_PASSWORD);
        }

        if (currTop.isPresent())
            nextRepeaterId = currTop.get().getRepeaterId() + 1;
        if(registerRemotePcDto.getGroupName().equals(defaultGroup)){
            throw new RemotePcException(ErrorCode.GROUP_NOT_SELECTED , "PC를 추가하려면 상단에서 그룹을 먼저 선택해주세요.");
        }
        Optional<Group> optionalGroup = groupRepository.findByGroupName(registerRemotePcDto.getGroupName());
        Group group = optionalGroup.orElseThrow(() -> new RemotePcException(ErrorCode.GROUP_NOT_FOUND));

        if (remotePcRepository.existsByNameAndGroup(registerRemotePcDto.getRemotePcName(), group)) {
            throw new RemotePcException(ErrorCode.PC_NAME_DUPLICATION);
        }

        RemotePc remotePc = RemotePc.createRemotePc(nextRepeaterId, registerRemotePcDto.getRemotePcName(), registerRemotePcDto.getAccessPassword(), group);



        remotePcRepository.save(remotePc);


    }

    @Transactional
    //클라이언드에서 돌아가기버튼 눌렀을 때 pc 상태 미배정으로 바뀌는 기능 추가
    public void cancelAssignment(RequestRemotePcDto remotePcDto){
        Optional<RemotePc> optionalRemotePc = remotePcRepository.findByName(remotePcDto.getPcName());

        RemotePc remotePc = optionalRemotePc.orElseThrow(() -> new RemotePcException(ErrorCode.PC_NOT_FOUND));
        if (!remotePcDto.getAccessPassword().equals(remotePc.getAccessPassword()))
            throw new RemotePcException(ErrorCode.PASSWORD_NOT_MATCH);
        remotePc.cancelAssignment();
        remotePcRepository.save(remotePc);
    }

    public ResponseRemotePcApiDto findRemotePcByPcName(RequestRemotePcDto remotePcDto){

        Optional<RemotePc> optionalRemotePc = remotePcRepository.findByName(remotePcDto.getPcName());
        RemotePc remotePc = optionalRemotePc.orElseThrow(() -> new RemotePcException(ErrorCode.PC_NOT_FOUND));
        if (!remotePcDto.getAccessPassword().equals(remotePc.getAccessPassword()))
            throw new RemotePcException(ErrorCode.PASSWORD_NOT_MATCH);
        remotePc.assign();
        remotePcRepository.save(remotePc);
        return new ResponseRemotePcApiDto(remotePc.getRepeaterId());
    }

    public Optional<RemotePc> findRemotePcByRepeaterId(Long repeaterId) {
        return remotePcRepository.findByRepeaterId(repeaterId);
    }

    @Transactional
    public void setRemotePcStatus(Long repeaterId, Status status) {
        Optional<RemotePc> optionalRemotePc = remotePcRepository.findByRepeaterId(repeaterId);
        RemotePc remotePc = optionalRemotePc.orElseThrow(() -> new RemotePcException(ErrorCode.PC_NOT_FOUND));
        remotePc.updateStatus(status);
        remotePcRepository.save(remotePc);
    }

    public RemotePc findById(User user,Long id) {
        Optional<RemotePc> optionalRemotePc =  remotePcRepository.findById(id);
        RemotePc remotePc = optionalRemotePc.orElseThrow(() -> new RemotePcException(ErrorCode.PC_NOT_FOUND));
        if(remotePc.getStatus() == Status.OFFLINE_NON_ASSIGNED){
            throw new RemotePcException(ErrorCode.UNASSIGNED_STATUS,"페이지를 새로고침 해주세요.");
        }

        Optional<Member> optionalMember = memberRepository.findByUsername(user.getUsername());
        Member member = optionalMember.orElseThrow(() -> new UsernameNotFoundException("코드를 수정하여 존재하지 않는 멤버에 대한 접근을 막으세요"));


        List<Member_Group> MyGroups = member_groupRepository.findByMemberAndGroup(member,remotePc.getGroup());
        if(MyGroups.isEmpty()){
            throw new RemotePcException(ErrorCode.OWN_GROUP_ONLY);
        }

        return remotePc;
    }

    @Transactional
    public ResponseDeleteDto deletePc(User user, Long remotePcId){

        Optional<RemotePc> optionalRemotePc =  remotePcRepository.findById(remotePcId);
        RemotePc remotePc = optionalRemotePc.orElseThrow(() -> new RemotePcException(ErrorCode.PC_NOT_FOUND," 페이지를 새로고침 해주세요."));
        if(remotePc.getStatus() != Status.OFFLINE_NON_ASSIGNED){
            throw new RemotePcException(ErrorCode.DELETE_ONLY_WHEN_UNASSIGNED,"페이지를 새로고침 해주세요.");
        }

        Optional<Member> optionalMember = memberRepository.findByUsername(user.getUsername());
        Member member = optionalMember.orElseThrow(() -> new UsernameNotFoundException("코드를 수정하여 존재하지 않는 멤버에 대한 접근을 막으세요"));



        List<Member_Group> MyGroups = member_groupRepository.findByMemberAndGroup(member,remotePc.getGroup());
        if(MyGroups.isEmpty()){
            throw new RemotePcException(ErrorCode.OWN_GROUP_ONLY2);
        }
        remotePcRepository.deleteById(remotePcId);
        return new ResponseDeleteDto(remotePc.getGroup().getGroupName());
    }

}
