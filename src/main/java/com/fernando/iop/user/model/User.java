package com.fernando.iop.user.model;

import com.fernando.iop.user.enums.UserRoles;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Entity
@Table(name = "user_table")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Size(max = 128)
    @Column(unique = true, nullable = false)
    private String userEmail;

    @Size(min = 6, max = 15)
    private String userPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = false)
    private UserRoles userRoles;

    @Column(nullable = false)
    private UUID projectId;

    public User(String userEmail, String userPassword, UserRoles userRoles, UUID projectId) {
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.userRoles = userRoles;
        this.projectId = projectId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public UserRoles getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(UserRoles userRoles) {
        this.userRoles = userRoles;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }
}
