package ru.mtkp.idm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
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
	 * Находит учётные записи по пользователю.
	 *
	 * @param userId идентификатор пользователя
	 * @return список учётных записей
	 */
	java.util.List<Account> findByUserId(Long userId);

	/**
	 * Находит учётные записи по статусу провижининга.
	 *
	 * @param provisioningStatus статус провижининга
	 * @return список учётных записей
	 */
	java.util.List<Account> findByProvisioningStatus(ru.mtkp.idm.model.ProvisioningStatus provisioningStatus);
}
