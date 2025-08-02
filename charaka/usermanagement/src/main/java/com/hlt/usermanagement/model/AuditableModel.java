package com.hlt.usermanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class AuditableModel<U> {

    @CreatedBy
    @Column(name = "CREATED_BY", updatable = false)
    protected U createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE", updatable = false)
    protected Date createdDate;

    @LastModifiedBy
    @Column(name = "MODIFIED_BY")
    protected U lastModifiedBy;

    @LastModifiedDate
    @Column(name = "MODIFIED_DATE")
    protected Date lastModifiedDate;
}
