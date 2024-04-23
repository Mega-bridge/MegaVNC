package kr.co.megabridge.megavnc.domain;


import jakarta.persistence.*;
import kr.co.megabridge.megavnc.enums.Role;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private String password;

    @NonNull
    @Enumerated(EnumType.STRING)
    private Role role;

    @NonNull
    @Column(unique = true)
    private String username;




    @PrePersist
    private void createdAt() {
        this.createdAt = new Date();
    }

    public static Member createMember(
            String username,
            String password,
            Role role,
            PasswordEncoder passwordEncoder
    ) {
        Member member = new Member();
        member.username = username;
        member.password = passwordEncoder.encode(password);
        member.role = role;
        return member;
    }

    public void changePassword(String password, PasswordEncoder passwordEncoder){
        this.password = passwordEncoder.encode(password);
    }

}
