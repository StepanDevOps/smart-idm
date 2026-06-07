package ru.mtkp.idm.model;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Сущность заявки IDM.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "idm_requests")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class IdmRequest {

	/**
	 * Идентификатор заявки.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	/**
	 * Пользователь, к которому относится заявка.
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id")
	@ToString.Exclude
	private User user;

	/**
	 * Целевая система.
	 */
	@Column(nullable = false, length = 120)
	private String targetSystem;

	/**
	 * Имя роли или операции.
	 */
	@Column(nullable = false, length = 255)
	private String roleName;

	/**
	 * Текущий статус заявки.
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private RequestStatus status;

	/**
	 * Время создания заявки.
	 */
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	/**
	 * Заполняет дату создания перед сохранением.
	 */
	@PrePersist
	void prePersist() {
		if (status == null) {
			status = RequestStatus.CREATED;
		}

		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}

