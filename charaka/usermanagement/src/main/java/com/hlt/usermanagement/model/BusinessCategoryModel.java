package com.hlt.usermanagement.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "business_categories")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BusinessCategoryModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;


}
