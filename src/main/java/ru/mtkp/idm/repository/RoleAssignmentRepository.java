package ru.mtkp.idm.repository;

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
}
