package kr.co.megabridge.megavnc.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "segment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @Column(unique = true)
    private String groupName;




}
