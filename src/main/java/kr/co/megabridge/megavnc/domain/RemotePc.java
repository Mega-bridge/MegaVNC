package kr.co.megabridge.megavnc.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class RemotePc {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String repeaterId;

    private String name;

    @ManyToOne
    private User owner;

    private Date createdAt;

    public static RemotePc createRemotePc(String repeaterId, String name, User owner) {
        RemotePc remotePc = new RemotePc();
        remotePc.repeaterId = repeaterId;
        remotePc.name = name;
        remotePc.owner = owner;
        return remotePc;
    }

    @PrePersist
    private void createdAt() {
        this.createdAt = new Date();
    }
}
