package com.juvarya.user.access.mgmt.model;

import com.juvarya.commonservice.enums.ERole;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
@Entity
@Table(name = "roles",
        indexes = {@Index(name = "idx_roleid", columnList = "id", unique = true)})
@Data
@Getter
@Setter
public class RoleModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ERole name;
}
