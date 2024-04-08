package kr.co.megabridge.megavnc.domain;


import jakarta.persistence.*;
import kr.co.megabridge.megavnc.enums.Role;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    private Date createdAt;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @NonNull
    @Enumerated(EnumType.STRING)
    private Role role;
    @NonNull
    @Column(unique = true)
    private String username;
    private User userDetail;


    @PrePersist
    private void createdAt() {
        this.createdAt = new Date();
    }

    public static Member createMember(
            String username,
            Role role,
            User user
    ) {
        Member member = new Member();
        member.username = username;
        member.role = role;
        member.userDetail = user;
        return member;
    }

}
