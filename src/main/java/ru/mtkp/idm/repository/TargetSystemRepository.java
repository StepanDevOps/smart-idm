package ru.mtkp.idm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ru.mtkp.idm.model.TargetSystem;

/**
 * Репозиторий целевых систем.
 */
@Repository
public interface TargetSystemRepository extends JpaRepository<TargetSystem, Integer> {

	/**
	 * Находит систему по имени.
	 *
	 * @param name имя системы
	 * @return найденная система
	 */
	Optional<TargetSystem> findByName(String name);

	/**
	 * Находит систему по ID с загрузкой ролей.
	 *
	 * @param id идентификатор системы
	 * @return найденная система с ролями
	 */
	@Query("SELECT ts FROM TargetSystem ts LEFT JOIN FETCH ts.roles WHERE ts.id = :id")
	Optional<TargetSystem> findByIdWithRoles(Integer id);

	/**
	 * Находит систему по ID с загрузкой аккаунтов.
	 *
	 * @param id идентификатор системы
	 * @return найденная система с аккаунтами
	 */
	@Query("SELECT ts FROM TargetSystem ts LEFT JOIN FETCH ts.accounts WHERE ts.id = :id")
	Optional<TargetSystem> findByIdWithAccounts(Integer id);

	/**
	 * Находит все роли для системы.
	 *
	 * @param systemId идентификатор системы
	 * @return список ролей
	 */
	@Query("SELECT r FROM Role r WHERE r.system.id = :systemId")
	List<ru.mtkp.idm.model.Role> findRolesBySystemId(Integer systemId);

	/**
	 * Находит все аккаунты для системы (с загрузкой user).
	 *
	 * @param systemId идентификатор системы
	 * @return список аккаунтов
	 */
	@Query("SELECT a FROM Account a JOIN FETCH a.user WHERE a.system.id = :systemId")
	List<ru.mtkp.idm.model.Account> findAccountsBySystemId(Integer systemId);
}
