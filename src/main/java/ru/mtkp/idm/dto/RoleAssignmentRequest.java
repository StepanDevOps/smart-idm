package ru.mtkp.idm.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса на назначение роли.
 * Используется для передачи данных при создании/обновлении назначения.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentRequest {

	/** Идентификатор пользователя */
	private Long userId;

	/** Идентификатор роли */
	private Integer roleId;

	/** Причина назначения */
	private String reason;

	/** Дата начала действия (null = с текущего дня) */
	private LocalDate effectiveFrom;

	/** Дата окончания действия (null = бессрочно) */
	private LocalDate effectiveTo;
}

