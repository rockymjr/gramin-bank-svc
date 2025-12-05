package com.graminbank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "pin", length = 4)
    private String pin;

    @Column(name = "is_operator")
    private Boolean isOperator = false;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_blocked")
    private Boolean isBlocked = false;

    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "last_failed_login")
    private LocalDateTime lastFailedLogin;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (joiningDate == null) {
            joiningDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method to check if user is currently blocked
    public boolean isCurrentlyBlocked() {
        if (!Boolean.TRUE.equals(isBlocked)) {
            return false;
        }
        if (blockedUntil == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(blockedUntil);
    }

    // Helper method to increment failed attempts
    public void incrementFailedAttempts() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null ? 0 : this.failedLoginAttempts) + 1;
        this.lastFailedLogin = LocalDateTime.now();

        // Block after 3 failed attempts
        if (this.failedLoginAttempts >= 3) {
            this.isBlocked = true;
            this.blockedUntil = LocalDateTime.now().plusDays(1);
        }
    }

    // Helper method to reset failed attempts on successful login
    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.lastFailedLogin = null;
    }
}