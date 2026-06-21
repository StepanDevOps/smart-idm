package ru.mtkp.idm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Сущность пользователя (таблица users).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

	/** Идентификатор */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	/** Логин (уникальный) */
	@Column(name = "login", nullable = false, unique = true, length = 50)
	private String login;

	/** Имя */
	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	/** Фамилия */
	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;

	/** Отчество */
	@Column(name = "middle_name", length = 100)
	private String middleName;

	/** Рабочая почта (уникальная) */
	@Column(name = "email_work", nullable = false, unique = true, length = 255)
	private String emailWork;

	/** Дата приема */
	@Column(name = "hire_date", nullable = false)
	private LocalDate hireDate;

	/** Дата увольнения */
	@Column(name = "termination_date")
	private LocalDate terminationDate;

	/** Статус пользователя */
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 32)
	private UserStatus status = UserStatus.ACTIVE;

	/** Дата создания записи */
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	/** Кто создал запись (ссылка на users.id) */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;

	/** Подразделение */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "department_id")
	private Department department;

	/** Прямой руководитель (линейный менеджер) */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "manager_id")
	private User manager;

	@PrePersist
	public void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
		validateDates();
	}

	@PreUpdate
	public void preUpdate() {
		validateDates();
	}

	private void validateDates() {
		if (terminationDate != null && hireDate != null && !terminationDate.isAfter(hireDate)) {
			throw new IllegalStateException("termination_date must be null or after hire_date");
		}
	}
}

