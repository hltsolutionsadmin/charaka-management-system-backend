package com.hlt.healthcare.model;

import com.hlt.healthcare.dto.enums.InteractionType;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "telecaller_interaction")
public class TelecallerInteractionModel extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "caller_user_id", nullable = false)
    private Long callerUserId;

    @Column(name = "telecaller_user_id", nullable = false)
    private Long telecallerUserId;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type")
    private InteractionType interactionType;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
