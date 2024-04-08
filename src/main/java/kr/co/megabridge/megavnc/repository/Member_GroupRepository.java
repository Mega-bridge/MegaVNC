package kr.co.megabridge.megavnc.repository;

import kr.co.megabridge.megavnc.domain.Group;
import kr.co.megabridge.megavnc.domain.Member;
import kr.co.megabridge.megavnc.domain.Member_Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Member_GroupRepository extends JpaRepository<Member_Group, Long> {

    List<Member_Group> findByMember(Member member);
    List<Member_Group> findByMemberAndGroup( Member member ,Group group);

    @Query("SELECT mg FROM Member_Group mg WHERE mg.group.id NOT IN (SELECT DISTINCT mg2.group.id FROM Member_Group mg2 WHERE mg2.member = :member)")
    List<Member_Group> findByMemberNotContaining(@Param("member") Member member);

    void deleteByMemberAndGroup(Member member ,Group group);
    void deleteAllByMember(Member member);
    void deleteAllByGroup(Group group);
}
