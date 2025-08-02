package com.juvarya.user.access.mgmt.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class ProjectLoginFlags {

    private Boolean skillrat = false;
    private Boolean yardly = false;
    private Boolean eato = false;
    private Boolean sancharalakshmi = false;
    private Boolean deliveryPartner = false;

}
