package kr.co.megabridge.megavnc.repository;


import kr.co.megabridge.megavnc.domain.Group;
import kr.co.megabridge.megavnc.domain.Member;
import kr.co.megabridge.megavnc.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByRole(Role role);
    Optional<Member> findByUsername(String username);
    @Query("SELECT m FROM Member m WHERE m.id != 1L")
    List<Member> findAllExceptIdOne();



}
