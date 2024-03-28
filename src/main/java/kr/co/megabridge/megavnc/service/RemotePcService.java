package kr.co.megabridge.megavnc.service;

import jakarta.transaction.Transactional;
import kr.co.megabridge.megavnc.domain.*;
import kr.co.megabridge.megavnc.dto.RequestRemotePcDto;
import kr.co.megabridge.megavnc.dto.ResponseRemotePcApiDto;
import kr.co.megabridge.megavnc.dto.ResponseRemotePcDto;
import kr.co.megabridge.megavnc.dto.responseDeleteDto;
import kr.co.megabridge.megavnc.enums.Status;
import kr.co.megabridge.megavnc.repository.GroupRepository;
import kr.co.megabridge.megavnc.repository.MemberRepository;
import kr.co.megabridge.megavnc.repository.Member_GroupRepository;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RemotePcService {

    private final RemotePcRepository remotePcRepository;
    private final MemberRepository memberRepository;
    private final Member_GroupRepository member_groupRepository;
    private final GroupRepository groupRepository;



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

        return responses;
    }

    public List<ResponseRemotePcDto> findByGroupName(String groupName ,User user){
        Optional<Group> optionalGroup = groupRepository.findByGroupName(groupName);
        List<ResponseRemotePcDto> responses = new ArrayList<>();
        if(optionalGroup.isEmpty()){
            throw new RuntimeException("해당 그룹을 추가해 주세요");
        }
        Group group = optionalGroup.get();
        Optional<Member> optionalMember = memberRepository.findByUsername(user.getUsername());
        if(optionalMember.isEmpty()){
            throw new RuntimeException("해당 유저가 존재하지 않습니다.");
        }
        Member member = optionalMember.get();

        List<Member_Group> myGroups = member_groupRepository.findByMemberAndGroup(member,group);
        if(myGroups.isEmpty()){
            throw new RuntimeException("자신의 그룹만 조회 가능합니다.");
        }

        List<RemotePc> remotePcs = remotePcRepository.findByGroup(group);
        for (RemotePc remotePc :remotePcs){
            ResponseRemotePcDto response = new ResponseRemotePcDto(remotePc.getId(),remotePc.getGroup(),remotePc.getName(),remotePc.getCreatedAt(),remotePc.getStatus());
            responses.add(response);
        }
        return responses;
    }

    public List<Group> findGroupByMember(User user){
        Optional<Member> optionalMember = memberRepository.findByUsername(user.getUsername());
        if (optionalMember.isEmpty()){
            throw new UsernameNotFoundException(user.getUsername()+"를 찾을 수 없습니다.");
        }
        Member member = optionalMember.get();
        List<Member_Group> myGroups = member_groupRepository.findByMember(member);
        List<Group> groups = new ArrayList<>();
        for(Member_Group member_group :myGroups){
            Group group = member_group.getGroup();
            groups.add(group);
        }

        return groups;
    }

    @Transactional
    //TODO: dto로 변경
    public void registerRemotePc(String remotePcName, String accessPassword,String groupName) {
        Optional<RemotePc> currTop = remotePcRepository.findTopByOrderByRepeaterIdDesc();

        // Make sure the repeater id starts from 100.
        // Base remote pc in data loader also ensures it.
        long nextRepeaterId = 100;
        if (currTop.isPresent())
            nextRepeaterId = currTop.get().getRepeaterId() + 1;
        if(groupName.equals("All Group")){
            throw new RuntimeException("그룹을 먼저 선택해 주세요");
        }
        Optional<Group> optionalGroup = groupRepository.findByGroupName(groupName);
        if (optionalGroup.isEmpty()){
            throw new RuntimeException("해당 그룹이 존재하지 않습니다");
        }
        Group group = optionalGroup.get();

        if (remotePcRepository.existsByNameAndGroup(remotePcName, group)) {
            throw new RuntimeException("동일한 이름의 RemotePc가 이미 존재합니다.");
        }

        RemotePc remotePc = RemotePc.createRemotePc(nextRepeaterId, remotePcName, accessPassword, group);



        remotePcRepository.save(remotePc);


    }

    @Transactional
    //클라이언드에서 돌아가기버튼 눌렀을 때 pc 상태 미배정으로 바뀌는 기능 추가
    public void cancelAssignment(RequestRemotePcDto remotePcDto){
        Optional<RemotePc> optionalRemotePc = remotePcRepository.findByName(remotePcDto.getPcName());
        if (optionalRemotePc.isEmpty()){
            throw new RuntimeException("존재하지 않는 remotePC에 대한 접근 요청 remotePcName : " + remotePcDto.getPcName());
        }
        RemotePc remotePc = optionalRemotePc.get();
        if (!remotePcDto.getAccessPassword().equals(remotePc.getAccessPassword()))
            throw new RuntimeException("접근 비밀번호가 일치하지 않습니다.");
        remotePc.cancelAssignment();
        remotePcRepository.save(remotePc);
    }

    public ResponseRemotePcApiDto findRemotePcByPcName(RequestRemotePcDto remotePcDto){

        Optional<RemotePc> optionalRemotePc = remotePcRepository.findByName(remotePcDto.getPcName());
        if (optionalRemotePc.isEmpty()){
            throw new RuntimeException("존재하지 않는 remotePC에 대한 접근 요청 remotePcName : " + remotePcDto.getPcName());
        }
        RemotePc remotePc = optionalRemotePc.get();
        if (!remotePcDto.getAccessPassword().equals(remotePc.getAccessPassword()))
            throw new RuntimeException("접근 비밀번호가 일치하지 않습니다.");
        remotePc.assign();
        remotePcRepository.save(remotePc);
        return new ResponseRemotePcApiDto(remotePc.getRepeaterId());
    }

    public Optional<RemotePc> findRemotePcByRepeaterId(Long repeaterId) {
        return remotePcRepository.findByRepeaterId(repeaterId);
    }




    //loading.html에 있었다면 컨트롤러의 showViewer 실행


    @Transactional
    public void setRemotePcStatus(Long repeaterId, Status status) {
        Optional<RemotePc> remotePc = remotePcRepository.findByRepeaterId(repeaterId);
        RemotePc update = remotePc.orElseThrow(() -> new NoSuchElementException("RemotePc not found for repeaterId: " + repeaterId +",status: "+status));
        //setter 사용 하면 안됨//TODO: 업데이트 매서드 만들기
        update.setStatus(status);
        remotePcRepository.save(update);
    }

    public RemotePc findById(User user,Long id) {
        Optional<RemotePc> optionalRemotePc =  remotePcRepository.findById(id);
        if(optionalRemotePc.isEmpty()){
            throw new RuntimeException("해당 PC가 존재하지 않습니다.");
        }
        RemotePc remotePc = optionalRemotePc.get();

        if(remotePc.getStatus() == Status.OFFLINE_NON_ASSIGNED){
            throw new RuntimeException("현재 미배정 상태입니다. 페이지를 새로고침 해주세요.");
        }

        Optional<Member> optionalMember = memberRepository.findByUsername(user.getUsername());
        if(optionalMember.isEmpty()){
            throw new RuntimeException("해당 유저가 존재하지 않습니다.");
        }
        Member member = optionalMember.get();

        List<Member_Group> MyGroups = member_groupRepository.findByMemberAndGroup(member,remotePc.getGroup());
        if(MyGroups.isEmpty()){
            throw new RuntimeException("자신의 그룹만 조회 가능합니다.");
        }

        return remotePc;
    }

    @Transactional
    public responseDeleteDto deletePc(User user, Long remotePcId){

        Optional<RemotePc> optionalRemotePc =  remotePcRepository.findById(remotePcId);
        if(optionalRemotePc.isEmpty()){
            throw new RuntimeException("해당 PC가 존재하지 않습니다. 페이지를 새로고침 해주세요.");
        }
        RemotePc remotePc = optionalRemotePc.get();
        if(remotePc.getStatus() != Status.OFFLINE_NON_ASSIGNED){
            throw new RuntimeException("미배정 상태일 때만 삭제가 가능합니다. 페이지를 새로고침 해주세요.");
        }

        Optional<Member> optionalMember = memberRepository.findByUsername(user.getUsername());
        if(optionalMember.isEmpty()){
            throw new RuntimeException("해당 유저가 존재하지 않습니다.");
        }
        Member member = optionalMember.get();

        List<Member_Group> MyGroups = member_groupRepository.findByMemberAndGroup(member,remotePc.getGroup());
        if(MyGroups.isEmpty()){
            throw new RuntimeException("자신의 그룹만 삭제 가능합니다.");
        }
        remotePcRepository.deleteById(remotePcId);
        return new responseDeleteDto(remotePc.getGroup().getGroupName());
    }

}
