package kr.co.megabridge.megavnc.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member_Group {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;


    @ManyToOne(fetch = FetchType.LAZY)
    private Group group;

    public static Member_Group assignGroup(Member member,Group group){
        Member_Group member_group =  new Member_Group();
        member_group.member = member;
        member_group.group = group;
        return member_group;
    }


}
