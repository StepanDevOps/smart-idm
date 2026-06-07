package ru.mtkp.idm.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Локальная учётная запись пользователя IDM.
 *
 * В таблице хранятся минимальные данные локального пользователя Smart IDM.
 * Пароль в открытом виде не сохраняется.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "idm_users", indexes = {
        @Index(name = "uq_idm_users_username", columnList = "username", unique = true)
})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class IdmUser {

    /** Идентификатор записи. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Уникальный логин пользователя IDM. */
    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    /** Полное имя пользователя IDM. */
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    /** Необратимый хеш пароля. */
    @JsonIgnore
    @ToString.Exclude
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** Роль пользователя IDM. */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private IdmUserRole role;

    /** Признак активности учётной записи. */
    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    /** Статус локальной учётной записи. */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private IdmUserStatus status = IdmUserStatus.ACTIVE;

    /** Дата и время создания записи. */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Дата и время последнего входа. */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    /** Дата и время смены пароля. */
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    /**
     * Устанавливает служебные поля перед созданием записи.
     */
    @PrePersist
    protected void prePersist() {
        var now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (passwordChangedAt == null) {
            passwordChangedAt = createdAt;
        }
        if (status == null) {
            status = IdmUserStatus.ACTIVE;
        }
    }
}

