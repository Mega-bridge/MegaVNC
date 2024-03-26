package kr.co.megabridge.megavnc.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class Member {
    private Date createdAt;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    @NonNull
    private String username;

    @NonNull
    private String password;

    @NonNull
    private String role;

    @NonNull
    private User userDetail;

    @JsonIgnore
    @ManyToOne
    private Segment group;

    @PrePersist
    private void createdAt() {
        this.createdAt = new Date();
    }

    public static Member createMember(
            String username,
            String rawPassword,
            String role,
            PasswordEncoder encoder,
            User user,
            Segment group
    ) {
        Member member = new Member();
        member.username = username;
        member.password = encoder.encode(rawPassword);
        member.role = role;
        member.userDetail = user;
        member.group = group;
        //유저 추가
        return member;
    }

}
