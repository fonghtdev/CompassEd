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
    
    @Column(name = "password_hash")
    private String passwordHash;
    
    private String provider; // "local", "google", etc.
    
    @Column(name = "provider_user_id")
    private String providerUserId; // OAuth provider user ID
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER; // Mặc định là USER
}
