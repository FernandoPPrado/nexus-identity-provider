package com.fernando.iop.user.model;

import com.fernando.iop.project.model.Project;
import com.fernando.iop.user.enums.UserRoles;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Entity
@Table(name = "user_table",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_email_project",
                        columnNames = {"userEmail", "project_id"}
                )
        }

)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Size(max = 128)
    @Column(nullable = false)
    private String userEmail;

    @Size(min = 6)
    private String userPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = false)
    private UserRoles userRoles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private boolean active;

    private String recoveryToken;

    private Instant recoveryTokenExpiry;

    @Column(nullable = false)
    private boolean confirmed;

    private String confirmToken;

    private Instant confirmTokenExpiry;

    public User(String userEmail, String userPassword, UserRoles userRoles, Project project) {
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.userRoles = userRoles;
        this.project = project;
        this.active = true;
        this.confirmed = false;
    }

    public User() {
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getRecoveryToken() {
        return recoveryToken;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public Instant getRecoveryTokenExpiry() {
        return recoveryTokenExpiry;
    }

    public void setRecoveryTokenExpiry(Instant recoveryTokenExpiry) {
        this.recoveryTokenExpiry = recoveryTokenExpiry;
    }

    public Instant getConfirmTokenExpiry() {
        return confirmTokenExpiry;
    }

    public void setConfirmTokenExpiry(Instant confirmTokenExpiry) {
        this.confirmTokenExpiry = confirmTokenExpiry;
    }

    public String getConfirmToken() {
        return confirmToken;
    }

    public void setConfirmToken(String confirmToken) {
        this.confirmToken = confirmToken;
    }

    public void setRecoveryToken(String recoveryToken) {
        this.recoveryToken = recoveryToken;
    }

    public Instant getRecoveryTokenExpirity() {
        return recoveryTokenExpiry;
    }

    public void setRecoveryTokenExpirity(Instant recoveryTokenExpirity) {
        this.recoveryTokenExpiry = recoveryTokenExpirity;
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
