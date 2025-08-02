package com.juvarya.user.access.mgmt.model;

import com.juvarya.commonservice.enums.UserVerificationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "USER", indexes = {
        @Index(name = "idx_userid", columnList = "id", unique = true)}, uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"), @UniqueConstraint(columnNames = "email")})
@Getter
@Setter
@Data
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "FIRST_NAME")
    private String fullName;

    @Size(max = 20)
    @Column(unique = true)
    private String username;

    @Size(max = 50)
    @Email
    private String email;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleModel> roleModels = new HashSet<>();

    @Column(name = "PROFILE_PICTURE")
    private Long profilePicture;

    @NotBlank
    @Column(name = "PRIMARY_CONTACT")
    private String primaryContact;

    @Column(name = "GENDER")
    private String gender;

    @Column(name = "CREATION_TIME")
    private Date creationTime;

    @Column(name = "TYPE")
    private String type;


    @Column(name = "FCM_TOKEN")
    private String fcmToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressModel> addresses;

    @Column(name = "JUVI_ID")
    private String juviId;


    @Column(name = "roll_number")
    private String rollNumber;

    @Column(name = "qualification")
    private String qualification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "b2b_unit_id")
    private B2BUnitModel b2bUnit;


    @OneToMany(mappedBy = "user")
    private List<ApiKeyModel> apiKeys;

    @Embedded
    private ProjectLoginFlags projectLoginFlags = new ProjectLoginFlags();

    @Column(name = "last_logout_date")
    private LocalDate lastLogOutDate;

    @Column(name = "recent_activity_date")
    private LocalDate recentActivityDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "student_verification_status", nullable = false)
    private UserVerificationStatus userVerificationStatus = UserVerificationStatus.NOT_VERIFIED;

    @Column(name = "branch")
    private String branch;

    @Column(name = "student_start_year")
    private Integer studentStartYear;

    @Column(name = "student_end_year")
    private Integer studentEndYear;

    @Column(name = "current_year")
    private Long currentYear;

    @Column(name = "password")
    private String password;


}