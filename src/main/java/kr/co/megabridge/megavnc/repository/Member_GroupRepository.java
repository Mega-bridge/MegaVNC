package kr.co.megabridge.megavnc.repository;

import kr.co.megabridge.megavnc.domain.Group;
import kr.co.megabridge.megavnc.domain.Member;
import kr.co.megabridge.megavnc.domain.Member_Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Member_GroupRepository extends JpaRepository<Member_Group, Long> {

    List<Member_Group> findByMember(Member member);
    List<Member_Group> findByMemberAndGroup( Member member ,Group group);
}
