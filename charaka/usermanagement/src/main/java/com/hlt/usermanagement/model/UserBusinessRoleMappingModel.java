package com.hlt.usermanagement.model;

import com.hlt.commonservice.enums.ERole;
import com.hlt.usermanagement.dto.enums.EMappingType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

/**
 * Reusable mapping to assign any user to a hospital (B2BUnit) with a specific role.
 *
 * Use Cases:
 * - Super Admin ➝ Onboards Hospital Admin
 * - Hospital Admin ➝ Onboards Telecaller / Receptionist
 * - A user can be mapped to multiple hospitals (with 2 max check for telecaller done in service)
 */
@Entity
@Table(name = "user_business_role_mappings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "b2b_unit_id", "role"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "is_active = true")
public class UserBusinessRoleMappingModel extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "b2b_unit_id", nullable = false)
    private B2BUnitModel b2bUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private ERole role;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @PrePersist
    public void prePersist() {
        if (isActive == null) {
            isActive = true;
        }
    }
}
