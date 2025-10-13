package com.jonyshev.accounts.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @PrePersist
    void pre() {
        if (id == null) id = UUID.randomUUID();
    }

    @Column(nullable = false, unique = true, length = 64)
    private String login;

    @Column(name = "pass_hash", nullable = false, length = 255)
    private String passHash;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false)
    private LocalDate birthdate;
}
