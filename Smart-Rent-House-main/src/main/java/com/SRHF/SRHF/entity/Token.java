package com.SRHF.SRHF.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="confirmation_token")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @Column(
            name="token",
            nullable = false
    )
    private String token;

    @Column(
            name="created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name="expires_at",
            nullable = false
    )
    private LocalDateTime expiresAt;

    @Column(
            name="confirmed_at"
    )
    private LocalDateTime confirmedAt;

    @ManyToOne
    @JoinColumn(
            nullable = false,
            name = "user_id"
    )
    private User user;

    // Add a public no-argument constructor required by JPA/Hibernate
    public Token() {
    }

    public Token(Long id) {
        this.id = id;
    }

    public Token(String token,
                 LocalDateTime createdAt,
                 LocalDateTime expiresAt,

                 User user) {
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
