package com.hlt.usermanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_attribute")
@Getter
@Setter
public class UserAttributeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attribute_name", nullable = false, length = 100)
    private String attributeName;

    @Column(name = "attribute_value", length = 500)
    private String attributeValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;
}
