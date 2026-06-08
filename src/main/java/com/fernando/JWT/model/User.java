package com.fernando.JWT.model;

import com.fernando.JWT.enums.UserRoles;
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
    private UUID project_id;


}
