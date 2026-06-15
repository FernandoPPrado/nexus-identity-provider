package com.fernando.iop.user.model;

import com.fernando.iop.project.model.Project;
import com.fernando.iop.user.enums.UserRoles;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.util.UUID;

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

    public User(String userEmail, String userPassword, UserRoles userRoles, Project project) {
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.userRoles = userRoles;
        this.project = project;
    }

    public User() {
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
