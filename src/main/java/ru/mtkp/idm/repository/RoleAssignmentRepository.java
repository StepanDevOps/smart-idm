package ru.mtkp.idm.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ru.mtkp.idm.model.RoleAssignment;

/**
 * Репозиторий назначений ролей.
 */
@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, Integer> {

	/**
	 * Находит назначения ролей по пользователю (с загрузкой роли).
	 *
	 * @param userId идентификатор пользователя
	 * @return список назначений ролей
	 */
	@Query("SELECT DISTINCT ra FROM RoleAssignment ra JOIN FETCH ra.role WHERE ra.user.id = :userId")
	List<RoleAssignment> findByUserId(Long userId);

	/**
	 * Находит активные назначения ролей по пользователю.
	 *
	 * @param userId идентификатор пользователя
	 * @return список активных назначений ролей
	 */
	@Query("SELECT DISTINCT ra FROM RoleAssignment ra JOIN FETCH ra.role WHERE ra.user.id = :userId AND ra.effectiveTo IS NULL")
	List<RoleAssignment> findByUserIdAndEffectiveToIsNull(Long userId);

	/**
	 * Находит истёкшие назначения ролей (где effectiveTo < сегодня).
	 *
	 * @return список истёкших назначений ролей
	 */
	@Query("SELECT DISTINCT ra FROM RoleAssignment ra JOIN FETCH ra.role WHERE ra.effectiveTo IS NOT NULL AND ra.effectiveTo < :today")
	List<RoleAssignment> findExpiredAssignments(LocalDate today);

	/**
	 * Находит назначение с загрузкой пользователя и роли.
	 *
	 * @param assignmentId идентификатор назначения
	 * @дополнительно Optional с назначением
	 */
	@Query("SELECT DISTINCT ra FROM RoleAssignment ra JOIN FETCH ra.user JOIN FETCH ra.role WHERE ra.id = :assignmentId")
	Optional<RoleAssignment> findByIdWithUserAndRole(Integer assignmentId);

	/**
	 * Находит пользователей с определённой ролью (активные назначения).
	 *
	 * @param roleId идентификатор роли
	 * @дополнительно список активных назначений с этой ролью
	 */
	@Query("SELECT DISTINCT ra FROM RoleAssignment ra JOIN FETCH ra.user JOIN FETCH ra.role WHERE ra.role.id = :roleId AND ra.effectiveTo IS NULL")
	List<RoleAssignment> findByRoleIdAndEffectiveToIsNull(Integer roleId);

	/**
	 * Находит все назначения с загрузкой пользователя и роли.
	 *
	 * @return список всех назначений
	 */
	@Query("SELECT DISTINCT ra FROM RoleAssignment ra JOIN FETCH ra.user JOIN FETCH ra.role ORDER BY ra.createdAt DESC")
	List<RoleAssignment> findAllWithUserAndRole();
}
