package ru.mtkp.idm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.mtkp.idm.model.RoleAssignment;

/**
 * Репозиторий назначений ролей.
 */
@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, Integer> {

	/**
	 * Находит назначения ролей по пользователю.
	 *
	 * @param userId идентификатор пользователя
	 * @return список назначений ролей
	 */
	java.util.List<RoleAssignment> findByUserId(Long userId);

	/**
	 * Находит активные назначения ролей по пользователю.
	 *
	 * @param userId идентификатор пользователя
	 * @return список активных назначений ролей
	 */
	java.util.List<RoleAssignment> findByUserIdAndEffectiveToIsNull(Long userId);
}
