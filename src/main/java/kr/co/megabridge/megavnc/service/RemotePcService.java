package kr.co.megabridge.megavnc.service;

import kr.co.megabridge.megavnc.domain.Member;
import kr.co.megabridge.megavnc.domain.RemotePc;
import kr.co.megabridge.megavnc.repository.MemberRepository;
import kr.co.megabridge.megavnc.security.User;
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

    public Long registerRemotePc(String remotePcName, Member owner) {
        Optional<RemotePc> currTop = remotePcRepository.findTopByOrderByRepeaterIdDesc();

        // Make sure the repeater id starts from 100.
        // Base remote pc in data loader also ensures it.
        long nextRepeaterId = 100;
        if (currTop.isPresent())
            nextRepeaterId = currTop.get().getRepeaterId() + 1;

        RemotePc remotePc = RemotePc.createRemotePc(nextRepeaterId, remotePcName, owner);
        remotePcRepository.save(remotePc);

        return nextRepeaterId;
    }

    public Optional<RemotePc> findRemotePcByRepeaterId(Long repeaterId) {
        return remotePcRepository.findByRepeaterId(repeaterId);
    }

    public void setRemotePcStatus(Long repeaterId, RemotePc.Status status) {
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
