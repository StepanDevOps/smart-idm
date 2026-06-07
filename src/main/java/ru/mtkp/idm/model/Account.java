package ru.mtkp.idm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Учётные записи в целевых системах (таблица accounts).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "accounts", indexes = {
        @Index(name = "idx_accounts_user_system", columnList = "user_id, system_id")
})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_id", nullable = false)
    private TargetSystem system;

    @Column(name = "account_login", nullable = false, length = 100)
    private String accountLogin;

    @Column(name = "status", nullable = false, length = 32)
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "password_expiry_date")
    private LocalDate passwordExpiryDate;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    @Column(name = "provisioning_status", nullable = false, length = 32)
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private ProvisioningStatus provisioningStatus = ProvisioningStatus.PENDING_CREATE;

    @PrePersist
    void prePersist() {
        if (provisioningStatus == null) {
            provisioningStatus = ProvisioningStatus.PENDING_CREATE;
        }
    }
}


