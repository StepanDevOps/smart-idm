package ru.mtkp.idm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Сущность пользователя IDM.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "idm_users")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

	/**
	 * Идентификатор пользователя.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	/**
	 * Уникальное имя учетной записи.
	 */
	@Column(nullable = false, unique = true, length = 100)
	private String username;

	/**
	 * Полное имя сотрудника.
	 */
	@Column(nullable = false, length = 255)
	private String fullName;

	/**
	 * Подразделение сотрудника.
	 */
	@Column(length = 255)
	private String department;

	/**
	 * Текущий статус пользователя.
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	@Default
	private UserStatus status = UserStatus.ACTIVE;
}

