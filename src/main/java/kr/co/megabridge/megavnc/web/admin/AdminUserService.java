package kr.co.megabridge.megavnc.web.admin;

import jakarta.transaction.Transactional;
import kr.co.megabridge.megavnc.domain.Member;
import kr.co.megabridge.megavnc.dto.requests.UserRegisterDto;
import kr.co.megabridge.megavnc.enums.Role;
import kr.co.megabridge.megavnc.exception.ErrorCode;
import kr.co.megabridge.megavnc.exception.exceptions.AdminUserException;
import kr.co.megabridge.megavnc.repository.MemberRepository;
import kr.co.megabridge.megavnc.repository.Member_GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final Member_GroupRepository member_groupRepository;


    public List<Member> listAllUsers() {
        return memberRepository.findAllExceptIdOne();
    }

    @Transactional
    public void register(UserRegisterDto user) {

        if (!user.getPassword().equals(user.getPasswordConfirm())) {
            throw new AdminUserException(ErrorCode.PASSWORD_NOT_CONFIRMED);
        }

        //입력한 사용자 이름이 이미 존재하는지 확인
        memberRepository.findByUsername(user.getUsername()).ifPresent(existingUser -> {
            throw new AdminUserException(ErrorCode.USER_ALREADY_EXIST);
        });

        Member member = Member.createMember(user.getUsername(), user.getPassword(), Role.ROLE_USER, passwordEncoder);
        memberRepository.save(member);
    }


    @Transactional
    public void deleteUser(Long memberId) {

        Optional<Member> optionalMember = memberRepository.findById(memberId);
        Member member = optionalMember.orElseThrow(() -> new AdminUserException(ErrorCode.USER_NOT_FOUND));
        if (member.getRole() == Role.ROLE_ADMIN) {
            throw new AdminUserException(ErrorCode.ADMIN_CANNOT_DELETE);
        }
        member_groupRepository.deleteAllByMember(member);
        memberRepository.delete(member);
    }


}
