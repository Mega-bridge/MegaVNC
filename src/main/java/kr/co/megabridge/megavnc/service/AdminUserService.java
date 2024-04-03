package kr.co.megabridge.megavnc.service;

import kr.co.megabridge.megavnc.domain.Member;
import kr.co.megabridge.megavnc.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final MemberRepository memberRepository;

    public List<Member> listAllUsers(){
        return memberRepository.findAll();
    }
}
