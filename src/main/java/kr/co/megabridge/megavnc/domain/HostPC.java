package kr.co.megabridge.megavnc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@Data
@Entity
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class HostPC {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Date createdAt;

    @NotBlank
    private final String name;

    @NotBlank
    private final String host;

    @NotBlank
    private final String port;

    @PrePersist
    void createdAt() {
        this.createdAt = new Date();
    }
}
