package ru.mtkp.idm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ru.mtkp.idm.model.Role;

/**
 * Репозиторий ролей.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

	/**
	 * Находит роль по имени.
	 *
	 * @param name имя роли
	 * @return найденная роль
	 */
	Optional<Role> findByName(String name);

	/**
	 * Находит все роли для системы.
	 *
	 * @param systemId идентификатор системы
	 * @return список ролей
	 */
	List<Role> findBySystemId(Integer systemId);

	/**
	 * Находит все роли с загрузкой системы.
	 *
	 * @return список всех ролей
	 */
	@Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.system ORDER BY r.name")
	List<Role> findAllWithSystem();
}
