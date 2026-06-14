package ru.mtkp.idm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ru.mtkp.idm.model.Account;
import ru.mtkp.idm.model.AccountStatus;

/**
 * Репозиторий учётных записей в целевых системах.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

	/**
	 * Находит учётную запись по пользователю и системе.
	 *
	 * @param userId идентификатор пользователя
	 * @param systemId идентификатор системы
	 * @return найденная учётная запись
	 */
	Optional<Account> findByUserIdAndSystemId(Long userId, Integer systemId);

	/**
	 * Находит учётные записи по пользователю (с загрузкой системы).
	 *
	 * @param userId идентификатор пользователя
	 * @return список учётных записей
	 */
	@Query("SELECT DISTINCT a FROM Account a JOIN FETCH a.system WHERE a.user.id = :userId")
	List<Account> findByUserId(Long userId);

	/**
	 * Находит учётную запись с загрузкой связанных сущностей.
	 *
	 * @param accountId идентификатор аккаунта
	 * @return найденная учётная запись с user и system
	 */
	@Query("SELECT a FROM Account a JOIN FETCH a.user JOIN FETCH a.system WHERE a.id = :accountId")
	Optional<Account> findByIdWithUserAndSystem(Integer accountId);

	/**
	 * Находит учётные записи по статусу провижининга.
	 *
	 * @param provisioningStatus статус провижининга
	 * @return список учётных записей
	 */
	List<Account> findByProvisioningStatus(ru.mtkp.idm.model.ProvisioningStatus provisioningStatus);
}
