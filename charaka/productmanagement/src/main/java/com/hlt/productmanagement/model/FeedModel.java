package com.hlt.productmanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "FEED", indexes = {@Index(name = "idx_feedid", columnList = "id", unique = true)})
@Getter
@Setter
public class FeedModel {

    @Id
    private Long id;

    @Column(name = "CREATION_TIME")
    private Date creationTime;

    @Column(name = "MODIFICATION_TIME")
    private Date modificationTime;

    @Column(name = "DESCRIPTION")
    private String description;


}
