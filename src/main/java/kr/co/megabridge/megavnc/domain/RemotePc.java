package kr.co.megabridge.megavnc.domain;

import jakarta.persistence.*;
import kr.co.megabridge.megavnc.enums.Status;
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

    @Column(unique = true)
    private String name;

    @ManyToOne
    private Member owner;

    private Date createdAt;

    private Status status;
    private String accessPassword;


    public static RemotePc createRemotePc(Long repeaterId, String name, String accessPassword ,Member owner) {
        RemotePc remotePc = new RemotePc();
        remotePc.repeaterId = repeaterId;
        remotePc.name = name;
        remotePc.owner = owner;
        remotePc.status = Status.OFFLINE_NON_ASSIGNED;
        remotePc.accessPassword = accessPassword;
        return remotePc;
    }
    public void cancelAssignment(){
        this.status = Status.OFFLINE_NON_ASSIGNED;
    }
    public void assign(){
        this.status = Status.OFFLINE_ASSIGNED;
    }

    @PrePersist
    private void createdAt() {
        this.createdAt = new Date();
    }
}
