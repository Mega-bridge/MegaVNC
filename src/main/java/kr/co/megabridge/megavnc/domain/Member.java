package kr.co.megabridge.megavnc.domain;


import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@NoArgsConstructor
public class Member {
    private Date createdAt;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @NonNull
    private String role;

    @NonNull
    private String username;


    private User userDetail;


    @PrePersist
    private void createdAt() {
        this.createdAt = new Date();
    }

    public static Member createMember(
            String username,
            String role,
            User user
    ) {
        Member member = new Member();
        member.username = username;
        member.role = role;
        member.userDetail = user;
        //유저 추가
        return member;
    }

}
