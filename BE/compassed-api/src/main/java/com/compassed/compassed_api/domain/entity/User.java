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

    // Bổ sung setter setProvider
    public void setProvider(String provider) {
        this.oauthProvider = provider;
    }
    // ...existing code...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getOauthProvider() { return oauthProvider; }
    public void setOauthProvider(String oauthProvider) { this.oauthProvider = oauthProvider; }
    public String getOauthProviderUserId() { return oauthProviderUserId; }
    public void setOauthProviderUserId(String oauthProviderUserId) { this.oauthProviderUserId = oauthProviderUserId; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
}
