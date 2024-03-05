package kr.co.megabridge.megavnc.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity(name = "remote_pc")
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class RemotePc {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private Long repeaterId;

    private String name;

    @ManyToOne
    private User owner;

    private Date createdAt;

    private Status status;

    public static enum Status {
        OFFLINE, STANDBY, ACTIVE
    }

    public static RemotePc createRemotePc(Long repeaterId, String name, User owner) {
        RemotePc remotePc = new RemotePc();
        remotePc.repeaterId = repeaterId;
        remotePc.name = name;
        remotePc.owner = owner;
        remotePc.status = Status.OFFLINE;
        return remotePc;
    }

    @PrePersist
    private void createdAt() {
        this.createdAt = new Date();
    }
}
