package ru.mtkp.idm.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.mtkp.idm.model.SecurityLog;

/**
 * Репозиторий логов аудита безопасности.
 */
@Repository
public interface SecurityLogRepository extends JpaRepository<SecurityLog, Integer> {

	/**
	 * Находит логи по пользователю.
	 *
	 * @param userId идентификатор пользователя
	 * @return список логов
	 */
	List<SecurityLog> findByUserId(Long userId);

	/**
	 * Находит логи за период времени.
	 *
	 * @param from начало периода
	 * @param to конец периода
	 * @return список логов
	 */
	List<SecurityLog> findByEventTimeBetween(LocalDateTime from, LocalDateTime to);

	/**
	 * Находит логи по типу события.
	 *
	 * @param eventType тип события
	 * @return список логов
	 */
	List<SecurityLog> findByEventType(String eventType);
}
