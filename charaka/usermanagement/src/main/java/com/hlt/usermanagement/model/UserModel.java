package com.hlt.usermanagement.model;

import com.hlt.auth.EncryptedStringConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "B2B_USER", uniqueConstraints = {
        @UniqueConstraint(columnNames = "USERNAME"),
        @UniqueConstraint(columnNames = "EMAIL_HASH"),
        @UniqueConstraint(columnNames = "PRIMARY_CONTACT_HASH")
})
@Getter
@Setter
public class UserModel extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, updatable = false)
    private Long id;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "FULL_NAME", nullable = false, length = 150)
    private String fullName;

    @Size(max = 20)
    @Column(name = "USERNAME", nullable = false, unique = true, length = 20)
    private String username;

    @Email
    @Size(max = 50)
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "EMAIL", nullable = false, length = 50)
    private String email;

    @Column(name = "EMAIL_HASH", nullable = false, unique = true)
    private String emailHash;

    @NotBlank
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "PRIMARY_CONTACT", nullable = false)
    private String primaryContact;

    @Column(name = "PRIMARY_CONTACT_HASH", nullable = false, unique = true)
    private String primaryContactHash;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "GENDER")
    private String gender;

    @Column(name = "PROFILE_PICTURE_ID")
    private Long profilePictureId;

    @Column(name = "FCM_TOKEN", length = 255)
    private String fcmToken;

    @Column(name = "JUVI_ID", length = 50)
    private String juviId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "USER_ROLES",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "ROLE_ID"))
    private Set<RoleModel> roleModels = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressModel> addresses;

    @Column(name = "RECENT_ACTIVITY_DATE")
    private LocalDate recentActivityDate;

    @Size(max = 255)
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "LAST_LOGOUT_DATE")
    private LocalDate lastLogOutDate;

    @Column(name = "CREATION_TIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime = new Date();

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<B2BUnitModel> businesses = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserAttributeModel> attributes = new HashSet<>();

    // Password reset token
    @Size(max = 255)
    @Column(name = "RESET_TOKEN")
    private String resetToken;

    @Column(name = "RESET_TOKEN_EXPIRY")
    private LocalDateTime resetTokenExpiry;


}
