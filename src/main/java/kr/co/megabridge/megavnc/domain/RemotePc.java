package kr.co.megabridge.megavnc.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import kr.co.megabridge.megavnc.enums.Status;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Entity(name = "remote_pc")
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class RemotePc {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private Long repeaterId;


    private String name;



    private Date createdAt;

    private Status status;
    private String accessPassword;


    @ManyToOne(fetch = FetchType.LAZY)
    private Group group;

    public static RemotePc createRemotePc(
            Long repeaterId,
            String name,
            String accessPassword,
            Group group
    ) {
        RemotePc remotePc = new RemotePc();
        remotePc.repeaterId = repeaterId;
        remotePc.name = name;
        remotePc.status = Status.OFFLINE_NON_ASSIGNED;
        remotePc.accessPassword = accessPassword;
        remotePc.group = group;
        return remotePc;
    }
    public void cancelAssignment(){
        this.status = Status.OFFLINE_NON_ASSIGNED;
    }
    public void assign(){
        this.status = Status.OFFLINE_ASSIGNED;
    }
    public void updateStatus(Status status){this.status = status;}

    @PrePersist
    private void createdAt() {
        this.createdAt = new Date();
    }
}
