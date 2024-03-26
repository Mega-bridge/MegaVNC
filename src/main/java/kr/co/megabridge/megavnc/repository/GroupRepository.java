package kr.co.megabridge.megavnc.repository;


import kr.co.megabridge.megavnc.domain.Segment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Segment, Long> {

    Segment findBySegmentName(String groupName);

}
