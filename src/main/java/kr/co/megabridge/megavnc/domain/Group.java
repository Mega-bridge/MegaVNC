package kr.co.megabridge.megavnc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity(name = "segment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Group {
    private Date createdAt;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @Column(unique = true)
    private String groupName;


    @PrePersist
    private void createdAt() {
        this.createdAt = new Date();
    }

    public static Group createGroup(String groupName){
        Group group = new Group();
        group.groupName = groupName;
        return group;
    }
}
