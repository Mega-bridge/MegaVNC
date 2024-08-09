package kr.co.megabridge.megavnc.service;

import jakarta.transaction.Transactional;
import kr.co.megabridge.megavnc.domain.*;
import kr.co.megabridge.megavnc.dto.*;
import kr.co.megabridge.megavnc.exception.ErrorCode;
import kr.co.megabridge.megavnc.enums.Status;
import kr.co.megabridge.megavnc.exception.exceptions.ApiException;
import kr.co.megabridge.megavnc.exception.exceptions.RemotePcException;
import kr.co.megabridge.megavnc.repository.GroupRepository;
import kr.co.megabridge.megavnc.repository.MemberRepository;
import kr.co.megabridge.megavnc.repository.Member_GroupRepository;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import kr.co.megabridge.megavnc.security.User;
import kr.co.megabridge.megavnc.websocket.RemotePcStatusHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class RemotePcService {
    private static final String ALL_GROUP = "All Group";

    private final RemotePcRepository remotePcRepository;
    private final MemberRepository memberRepository;
    private final Member_GroupRepository member_groupRepository;
    private final GroupRepository groupRepository;
    private final RemotePcStatusHandler remotePcStatusHandler;

    @Autowired
    public RemotePcService(RemotePcRepository remotePcRepository,
                           MemberRepository memberRepository,
                           Member_GroupRepository member_groupRepository,
                           GroupRepository groupRepository,
                           RemotePcStatusHandler remotePcStatusHandler) {
        this.remotePcRepository = remotePcRepository;
        this.memberRepository = memberRepository;
        this.member_groupRepository = member_groupRepository;
        this.groupRepository = groupRepository;
        this.remotePcStatusHandler = remotePcStatusHandler;
    }


    public List<ResponseRemotePcDto> findByGroups(List<Group> groups) {
        List<ResponseRemotePcDto> responses = new ArrayList<>();
        for (Group group : groups) {
            List<RemotePc> remotePcs = remotePcRepository.findByGroup(group);
            for (RemotePc remotePc : remotePcs) {
                ResponseRemotePcDto response = new ResponseRemotePcDto(remotePc.getId(), remotePc.getGroup(), remotePc.getName(), remotePc.getAssignedAt(), remotePc.getStatus());
                responses.add(response);
            }
        }
        Collections.sort(responses, Comparator.comparing(ResponseRemotePcDto::getId));
        return responses;
    }

    public List<ResponseRemotePcDto> findByGroupName(String groupName, User user) {
        Optional<Group> optionalGroup = groupRepository.findByGroupName(groupName);
        List<ResponseRemotePcDto> responses = new ArrayList<>();
        Group group = optionalGroup.orElseThrow(() -> new RemotePcException(ErrorCode.GROUP_NOT_FOUND, "해당 그룹을 추가해 주세요."));
        Optional<Member> optionalMember = memberRepository.findByUsername(user.getUsername());
        Member member = optionalMember.orElseThrow(() -> new UsernameNotFoundException("코드를 수정하여 존재하지 않는 멤버에 대한 접근을 막으세요"));


        List<Member_Group> myGroups = member_groupRepository.findByMemberAndGroup(member, group);
        if (myGroups.isEmpty()) {
            throw new RemotePcException(ErrorCode.OWN_GROUP_ONLY);
        }

        List<RemotePc> remotePcs = remotePcRepository.findByGroup(group);
        for (RemotePc remotePc : remotePcs) {
            ResponseRemotePcDto response = new ResponseRemotePcDto(remotePc.getId(), remotePc.getGroup(), remotePc.getName(), remotePc.getAssignedAt(), remotePc.getStatus());
            responses.add(response);
        }
        Collections.sort(responses, Comparator.comparing(ResponseRemotePcDto::getId));
        return responses;
    }

    public List<Group> findGroupByMember(User user) {
        Optional<Member> optionalMember = memberRepository.findByUsername(user.getUsername());

        Member member = optionalMember.orElseThrow(() -> new UsernameNotFoundException("코드를 수정하여 존재하지 않는 멤버에 대한 접근을 막으세요"));
        List<Member_Group> myGroups = member_groupRepository.findByMember(member);
        List<Group> groups = new ArrayList<>();
        for (Member_Group member_group : myGroups) {
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
        if (registerRemotePcDto.getRemotePcName().isEmpty()) {
            throw new RemotePcException(ErrorCode.MISSING_PC_NAME);
        }
        if (registerRemotePcDto.getAccessPassword().isEmpty()) {
            throw new RemotePcException(ErrorCode.MISSING_ACCESS_PASSWORD);
        }

        if (currTop.isPresent())
            nextRepeaterId = currTop.get().getRepeaterId() + 1;
        if (registerRemotePcDto.getGroupName().equals(ALL_GROUP)) {
            throw new RemotePcException(ErrorCode.GROUP_NOT_SELECTED, "PC를 추가하려면 상단에서 그룹을 먼저 선택해주세요.");
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
    public void setRemotePcStatus(RemotePc remotePc, Status status) {

        remotePc.updateStatus(status);
        if (remotePc.getAssignedAt() == null && status == Status.STANDBY) {
            // remotePc 등록
            remotePc.assign();
        }
        Date assignedAt = remotePc.getAssignedAt();
        //웹소켓으로 웹의 상태 즉시 변경

        String formattedAssignedAt = "";
        if (assignedAt != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            formattedAssignedAt = formatter.format(assignedAt);
        }

        remotePcStatusHandler.sendStatusUpdate(remotePc.getId(), Status.toValue(status), formattedAssignedAt);
    }



    public RemotePc findById(User user, Long id) {
        Optional<RemotePc> optionalRemotePc = remotePcRepository.findById(id);
        RemotePc remotePc = optionalRemotePc.orElseThrow(() -> new RemotePcException(ErrorCode.PC_NOT_FOUND));
        if (remotePc.getStatus() == Status.OFFLINE) {
            throw new RemotePcException(ErrorCode.OFFLINE_STATUS, "페이지를 새로고침 해주세요.");
        }

        Optional<Member> optionalMember = memberRepository.findByUsername(user.getUsername());
        Member member = optionalMember.orElseThrow(() -> new UsernameNotFoundException("코드를 수정하여 존재하지 않는 멤버에 대한 접근을 막으세요"));


        List<Member_Group> MyGroups = member_groupRepository.findByMemberAndGroup(member, remotePc.getGroup());
        if (MyGroups.isEmpty()) {
            throw new RemotePcException(ErrorCode.OWN_GROUP_ONLY);
        }

        return remotePc;
    }

    @Transactional
    public ResponseDeleteDto deletePc(User user, Long remotePcId) {

        Optional<RemotePc> optionalRemotePc = remotePcRepository.findById(remotePcId);
        RemotePc remotePc = optionalRemotePc.orElseThrow(() -> new RemotePcException(ErrorCode.PC_NOT_FOUND, " 페이지를 새로고침 해주세요."));
        if (remotePc.getStatus() == Status.ACTIVE) {
            throw new RemotePcException(ErrorCode.DELETE_NOT_ONLY_WHEN_ACTIVE, "페이지를 새로고침 해주세요.");
        }

        if (remotePc.getId() == 1L) {
            throw new RemotePcException(ErrorCode.CANNOT_DELETE_DEFAULT_PC);

        }
        if (remotePc.getAssignedAt() != null) {
            throw new RemotePcException(ErrorCode.CANNOT_DELETE_ASSIGNED_PC);
        }

        Optional<Member> optionalMember = memberRepository.findByUsername(user.getUsername());
        Member member = optionalMember.orElseThrow(() -> new UsernameNotFoundException("코드를 수정하여 존재하지 않는 멤버에 대한 접근을 막으세요"));


        List<Member_Group> MyGroups = member_groupRepository.findByMemberAndGroup(member, remotePc.getGroup());
        if (MyGroups.isEmpty()) {
            throw new RemotePcException(ErrorCode.OWN_GROUP_ONLY2);
        }

        remotePcRepository.deleteById(remotePcId);
        return new ResponseDeleteDto(remotePc.getGroup().getGroupName());
    }

}
