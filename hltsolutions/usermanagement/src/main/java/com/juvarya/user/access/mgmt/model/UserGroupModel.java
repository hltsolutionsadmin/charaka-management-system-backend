package com.juvarya.user.access.mgmt.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "USER_GROUP",
        indexes = {@Index(name = "idx_usergroupid", columnList = "id", unique = true)})
@Getter
@Setter
public class UserGroupModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CODE")
    private String code;

    @Column(name = "PROFILE_ID")
    private Long profileId;


}
