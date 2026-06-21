package ru.mtkp.idm.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import ru.mtkp.idm.model.RoleAssignment;

/**
 * Сервис управления назначениями ролей.
 * Отвечает за назначение, отзыв и управление жизненным циклом ролей пользователей.
 */
public interface RoleAssignmentService {

	/**
	 * Назначает роль пользователю.
	 *
	 * @param userId идентификатор пользователя
	 * @param roleId идентификатор роли
	 * @param reason причина назначения
	 * @param effectiveFrom дата начала действия (null = с текущего дня)
	 * @param effectiveTo дата окончания действия (null = бессрочно)
	 * @return созданное назначение
	 */
	RoleAssignment assignRoleToUser(Long userId, Integer roleId, String reason, LocalDate effectiveFrom, LocalDate effectiveTo);

	/**
	 * Создает INDIRECT назначение через департамент.
	 *
	 * @param userId идентификатор пользователя
	 * @param roleId идентификатор роли
	 * @param departmentId идентификатор департамента
	 * @param reason причина назначения
	 * @return созданное назначение
	 */
	RoleAssignment createIndirectAssignment(Long userId, Integer roleId, Integer departmentId, String reason);

	/**
	 * Получает назначения по userId и roleId.
	 *
	 * @param userId идентификатор пользователя
	 * @param roleId идентификатор роли
	 * @return список назначений
	 */
	List<RoleAssignment> getAssignmentsByUserAndRole(Long userId, Integer roleId);

	/**
	 * Отзывает назначение роли по идентификатору.
	 * Устанавливает effectiveTo на вчерашний день.
	 *
	 * @param assignmentId идентификатор назначения
	 * @return true если назначение успешно отозвано
	 */
	boolean revokeRoleAssignment(Integer assignmentId);

	/**
	 * Получает все активные назначения ролей для пользователя.
	 * Активные = где effectiveTo IS NULL или effectiveTo >= today
	 *
	 * @param userId идентификатор пользователя
	 * @return список активных назначений ролей
	 */
	List<RoleAssignment> getActiveAssignments(Long userId);

	/**
	 * Получает все истёкшие назначения ролей.
	 * Истёкшие = где effectiveTo < today
	 *
	 * @return список истёкших назначений ролей
	 */
	List<RoleAssignment> getExpiredAssignments();

	/**
	 * Получает все назначения ролей для пользователя (включая истёкшие).
	 *
	 * @param userId идентификатор пользователя
	 * @return список всех назначений ролей
	 */
	List<RoleAssignment> getAllAssignments(Long userId);

	/**
	 * Получает назначение по идентификатору.
	 *
	 * @param assignmentId идентификатор назначения
	 * дополнительно Optional с назначением
	 */
	Optional<RoleAssignment> getAssignmentById(Integer assignmentId);

	/**
	 * Проверяет, имеет ли пользователь роль.
	 *
	 * @param userId идентификатор пользователя
	 * @param roleId идентификатор роли
	 * @return true если пользователь имеет активную роль
	 */
	boolean hasRole(Long userId, Integer roleId);

	/**
	 * Получает всех пользователей с определённой ролью.
	 *
	 * @param roleId идентификатор роли
	 * @return список пользователей с этой ролью
	 */
	List<RoleAssignment> getUsersWithRole(Integer roleId);

	/**
	 * Получает все назначения ролей для всех пользователей.
	 *
	 * @return список всех назначений ролей
	 */
	List<RoleAssignment> getAllAssignmentsForAllUsers();
}
