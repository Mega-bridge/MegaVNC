package kr.co.megabridge.megavnc.service;

import kr.co.megabridge.megavnc.domain.Member;
import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.dto.RequestRemotePcDto;
import kr.co.megabridge.megavnc.dto.ResponseRemotePcDto;
import kr.co.megabridge.megavnc.enums.Status;
import kr.co.megabridge.megavnc.repository.MemberRepository;
import kr.co.megabridge.megavnc.domain.User;
import kr.co.megabridge.megavnc.repository.RemotePcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RemotePcService {

    private final RemotePcRepository remotePcRepository;
    private final MemberRepository memberRepository;



    public Iterable<RemotePc> findAll() {
        return remotePcRepository.findAll();
    }

    public Iterable<RemotePc> findByOwner(User user) {
        Optional<Member> optionalMember = memberRepository.findByUsername(user.getUsername());
        if (optionalMember.isEmpty()){
            throw new UsernameNotFoundException(user.getUsername()+"를 찾을 수 없습니다.");
        }
        Member member = optionalMember.get();
        return remotePcRepository.findByOwner(member);
    }

    public void registerRemotePc(String remotePcName,String accessPassword, Member owner) {
        Optional<RemotePc> currTop = remotePcRepository.findTopByOrderByRepeaterIdDesc();

        // Make sure the repeater id starts from 100.
        // Base remote pc in data loader also ensures it.
        long nextRepeaterId = 100;
        if (currTop.isPresent())
            nextRepeaterId = currTop.get().getRepeaterId() + 1;

        RemotePc remotePc = RemotePc.createRemotePc(nextRepeaterId, remotePcName, accessPassword,owner);
        remotePcRepository.save(remotePc);


    }

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

    public ResponseRemotePcDto findRemotePcByPcName(RequestRemotePcDto remotePcDto){

        Optional<RemotePc> optionalRemotePc = remotePcRepository.findByName(remotePcDto.getPcName());
        if (optionalRemotePc.isEmpty()){
            throw new RuntimeException("존재하지 않는 remotePC에 대한 접근 요청 remotePcName : " + remotePcDto.getPcName());
        }
        RemotePc remotePc = optionalRemotePc.get();
        if (!remotePcDto.getAccessPassword().equals(remotePc.getAccessPassword()))
            throw new RuntimeException("접근 비밀번호가 일치하지 않습니다.");
        remotePc.assign();
        remotePcRepository.save(remotePc);
        return new ResponseRemotePcDto(remotePc.getRepeaterId());
    }

    public Optional<RemotePc> findRemotePcByRepeaterId(Long repeaterId) {
        return remotePcRepository.findByRepeaterId(repeaterId);
    }




    //loading.html에 있었다면 컨트롤러의 showViewer 실행



    public void setRemotePcStatus(Long repeaterId, Status status) {
        Optional<RemotePc> remotePc = remotePcRepository.findByRepeaterId(repeaterId);
        RemotePc update = remotePc.orElseThrow(() -> new NoSuchElementException("RemotePc not found for repeaterId: " + repeaterId +",status: "+status));
        //setter 사용 하면 안됨//TODO: 업데이트 매서드 만들기
        update.setStatus(status);
        remotePcRepository.save(update);
    }

    public RemotePc findById(Long id) {
        return remotePcRepository.findById(id).orElseThrow();
    }
}
