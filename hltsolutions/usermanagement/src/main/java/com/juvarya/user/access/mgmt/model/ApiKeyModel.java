package com.juvarya.user.access.mgmt.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_keys")
@Getter
@Setter
public class ApiKeyModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String hashedKey;

    @Column(nullable = false, unique = true)
    private String tenantKey;

    @Column(nullable = false)
    private String tenantDbName;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;
}
