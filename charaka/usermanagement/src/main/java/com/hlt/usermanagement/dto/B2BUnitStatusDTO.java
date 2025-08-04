package com.hlt.usermanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hlt.commonservice.dto.Role;
import com.hlt.usermanagement.dto.response.BusinessAttributeResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class B2BUnitStatusDTO {
    private Long id;
    private String businessName;
    private Boolean enabled;
    private Set<Role> roles;
    private Set<BusinessAttributeResponse> attributes;
    public static B2BUnitStatusDTO rolesOnly(Set<Role> roles) {
        B2BUnitStatusDTO dto = new B2BUnitStatusDTO();
        dto.setRoles(roles);
        return dto;
    }
}
