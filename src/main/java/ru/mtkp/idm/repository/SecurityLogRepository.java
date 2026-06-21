package ru.mtkp.idm.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

	/**
	 * Находит логи с пагинацией (сортировка по eventTime DESC).
	 *
	 * @param pageable параметры пагинации
	 * @return страница логов
	 */
	Page<SecurityLog> findAllByOrderByEventTimeDesc(Pageable pageable);

	/**
	 * Находит логи с фильтрами и пагинацией.
	 *
	 * @param eventType тип события (опционально)
	 * @param userId ID пользователя (опционально)
	 * @param fromDate начальная дата (опционально)
	 * @param toDate конечная дата (опционально)
	 * @param pageable параметры пагинации
	 * @return страница логов
	 */
	@Query("SELECT s FROM SecurityLog s WHERE " +
			"(:eventType IS NULL OR s.eventType = :eventType) AND " +
			"(:userId IS NULL OR s.user.id = :userId) AND " +
			"(:fromDate IS NULL OR s.eventTime >= :fromDate) AND " +
			"(:toDate IS NULL OR s.eventTime <= :toDate)")
	Page<SecurityLog> findByFilters(
			@Param("eventType") String eventType,
			@Param("userId") Long userId,
			@Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate,
			Pageable pageable);

	/**
	 * Находит все уникальные типы событий.
	 *
	 * @return список типов событий
	 */
	@Query("SELECT DISTINCT s.eventType FROM SecurityLog s ORDER BY s.eventType")
	List<String> findDistinctEventTypes();
}
