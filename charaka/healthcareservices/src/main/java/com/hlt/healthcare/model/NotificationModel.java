package com.hlt.healthcare.model;


import com.hlt.healthcare.firebase.dto.NotificationEventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "NOTIFICATION", indexes = {@Index(name = "idx_notificationid", columnList = "id", unique = true)})
@Getter
@Setter
public class NotificationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CREATION_TIME")
    private Date creationTime;

    @Column(name = "MODIFICATION_TIME")
    private Date modificationTime;

    @Column(name = "MESSAGE")
    private String message;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "TYPE")
    private NotificationEventType type;

    @Column(name = "USER_ID")
    private Long userId;

}
