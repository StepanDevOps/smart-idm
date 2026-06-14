package ru.mtkp.idm.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для назначения роли.
 * Используется для передачи данных между слоями приложения.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentDTO {

	/** Идентификатор назначения */
	private Integer id;

	/** Идентификатор пользователя */
	private Long userId;

	/** Имя пользователя */
	private String userName;

	/** Идентификатор роли */
	private Integer roleId;

	/** Название роли */
	private String roleName;

	/** Тип назначения (DIRECT, INDIRECT) */
	private String assignmentType;

	/** Дата начала действия */
	private LocalDate effectiveFrom;

	/** Дата окончания действия (null = бессрочно) */
	private LocalDate effectiveTo;

	/** Причина назначения */
	private String assignmentReason;

	/** Статус: активное (true) или истёкшее (false) */
	private Boolean active;
}
