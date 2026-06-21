package ru.mtkp.idm.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
	List<RoleAssignment> findByUserId(@Param("userId") Long userId);

	/**
	 * Находит активные назначения ролей по пользователю (учитывая флаг isActive).
	 *
	 * @param userId идентификатор пользователя
	 * @return список активных назначений ролей
	 */
	@Query("SELECT DISTINCT ra FROM RoleAssignment ra JOIN FETCH ra.role " +
			"WHERE ra.user.id = :userId AND (ra.isActive = true OR ra.isActive IS NULL)")
	List<RoleAssignment> findByUserIdAndEffectiveToIsNull(@Param("userId") Long userId);

	/**
	 * Находит истёкшие назначения ролей (где effectiveTo < сегодня и назначение активно).
	 *
	 * @param today текущая дата
	 * @return список истёкших назначений ролей
	 */
	@Query("SELECT DISTINCT ra FROM RoleAssignment ra JOIN FETCH ra.role " +
			"WHERE ra.effectiveTo IS NOT NULL AND ra.effectiveTo < :today AND (ra.isActive = true OR ra.isActive IS NULL)")
	List<RoleAssignment> findExpiredAssignments(@Param("today") LocalDate today);

	/**
	 * Находит назначение с загрузкой пользователя и роли по ID назначения.
	 *
	 * @param assignmentId идентификатор назначения
	 * @return Optional с назначением ролей
	 */
	@Query("SELECT DISTINCT ra FROM RoleAssignment ra JOIN FETCH ra.user JOIN FETCH ra.role WHERE ra.id = :assignmentId")
	Optional<RoleAssignment> findByIdWithUserAndRole(@Param("assignmentId") Integer assignmentId);

	/**
	 * Находит пользователей с определённой ролью (активные назначения, учитывая флаг isActive).
	 *
	 * @param roleId идентификатор роли
	 * @return список активных назначений с этой ролью
	 */
	@Query("SELECT DISTINCT ra FROM RoleAssignment ra JOIN FETCH ra.user JOIN FETCH ra.role " +
			"WHERE ra.role.id = :roleId AND (ra.isActive = true OR ra.isActive IS NULL)")
	List<RoleAssignment> findByRoleIdAndEffectiveToIsNull(@Param("roleId") Integer roleId);

	/**
	 * Находит все назначения с загрузкой пользователя и роли, отсортированные по дате создания.
	 *
	 * @return список всех назначений
	 */
	@Query("SELECT DISTINCT ra FROM RoleAssignment ra JOIN FETCH ra.user JOIN FETCH ra.role " +
			"ORDER BY ra.createdAt DESC")
	List<RoleAssignment> findAllWithUserAndRole();

	/**
	 * Находит назначения по департаменту (включая INDIRECT).
	 *
	 * @param departmentId идентификатор департамента
	 * @return список назначений
	 */
	@Query("SELECT DISTINCT ra FROM RoleAssignment ra " +
			"JOIN FETCH ra.user JOIN FETCH ra.role " +
			"LEFT JOIN FETCH ra.department " +
			"WHERE ra.department.id = :departmentId " +
			"AND (ra.assignmentType = 'INDIRECT' OR ra.isActive = true) " +
			"ORDER BY ra.createdAt DESC")
	List<RoleAssignment> findByDepartmentIdAndActive(@Param("departmentId") Integer departmentId);

	/**
	 * Находит INDIRECT назначения по департаменту.
	 *
	 * @param departmentId идентификатор департамента
	 * @return список INDIRECT назначений
	 */
	@Query("SELECT DISTINCT ra FROM RoleAssignment ra " +
			"JOIN FETCH ra.user JOIN FETCH ra.role " +
			"LEFT JOIN FETCH ra.department " +
			"WHERE ra.department.id = :departmentId " +
			"AND ra.assignmentType = 'INDIRECT' " +
			"ORDER BY ra.createdAt DESC")
	List<RoleAssignment> findIndirectAssignmentsByDepartment(@Param("departmentId") Integer departmentId);
}