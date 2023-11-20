package kr.co.megabridge.megavnc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class HostPC {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Date createdAt;

    @NotBlank
    private String name;

    @NotBlank
    private String host;

    @NotBlank
    private String port;

    @PrePersist
    void createdAt() {
        this.createdAt = new Date();
    }
}
