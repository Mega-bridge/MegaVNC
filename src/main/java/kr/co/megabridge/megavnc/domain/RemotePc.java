package kr.co.megabridge.megavnc.domain;

import jakarta.persistence.*;
import kr.co.megabridge.megavnc.enums.Status;
import lombok.*;

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

    private Date assignedAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String accessPassword;


    private String reconnectId;

    @ManyToOne
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
        remotePc.assignedAt = null;
        remotePc.status = Status.OFFLINE;
        remotePc.accessPassword = accessPassword;
        remotePc.group = group;
        remotePc.reconnectId = "";
        return remotePc;
    }

    public void assign(){
        this.assignedAt = new Date();
    }
    public void updateStatus(Status status){this.status = status;}
    public void updateReconnectId( String reconnectId){
        this.reconnectId = reconnectId;
    }

    public void disAssign(){
       // this.status = Status.OFFLINE;
        this.assignedAt = null;
        this.reconnectId ="";
    }



}
