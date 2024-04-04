package kr.co.megabridge.megavnc.repository;


import kr.co.megabridge.megavnc.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByGroupName(String groupName);

    @Query("SELECT g FROM segment g WHERE g.id >= :id")
    List<Group> findAllStartWith(@Param("id") Long id);

}
