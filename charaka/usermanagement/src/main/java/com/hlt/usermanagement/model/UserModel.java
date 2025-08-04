package com.hlt.usermanagement.model;

import com.hlt.auth.EncryptedStringConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "B2B_USER", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
@Getter
@Setter
public class UserModel extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "FULL_NAME", nullable = false)
    private String fullName;

    @Size(max = 20)
    @Column(name = "USERNAME", unique = true, nullable = false)
    private String username;

    @Email
    @Size(max = 50)
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "EMAIL", unique = true, nullable = false)
    private String email;

    @Column(name = "email_hash", unique = true)
    private String emailHash;

    @NotBlank
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "PRIMARY_CONTACT", nullable = false)
    private String primaryContact;

    @Column(name = "primary_contact_hash")
    private String primaryContactHash;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "GENDER")
    private String gender;

    @Column(name = "PROFILE_PICTURE_ID")
    private Long profilePictureId;

    @Column(name = "FCM_TOKEN")
    private String fcmToken;

    @Column(name = "JUVI_ID")
    private String juviId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "USER_ROLES",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "ROLE_ID"))
    private Set<RoleModel> roleModels = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressModel> addresses;

    @Column(name = "recent_activity_date")
    private LocalDate recentActivityDate;

    @Size(max = 50)
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "password", unique = true, nullable = false)
    private String password;

    @Column(name = "last_logout_date")
    private LocalDate lastLogOutDate;

    @Column(name = "CREATION_TIME")
    private Date creationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "b2b_unit_id")
    private B2BUnitModel b2bUnit;
}
