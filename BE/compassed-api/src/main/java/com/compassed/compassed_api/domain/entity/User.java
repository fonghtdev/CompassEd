package com.compassed.compassed_api.domain.entity;

import com.compassed.compassed_api.domain.enums.UserRole;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter @Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique = true)
    private String email;

    private String fullName;

    @Column(length = 255)
    private String passwordHash;

    @Column(length = 50)
    private String oauthProvider;

    @Column(length = 255)
    private String oauthProviderUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;
}
