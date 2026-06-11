package ru.mtkp.idm.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.mtkp.idm.model.SecurityLog;
import ru.mtkp.idm.model.User;
import ru.mtkp.idm.model.UserStatus;
import ru.mtkp.idm.repository.SecurityLogRepository;
import ru.mtkp.idm.repository.UserRepository;
import ru.mtkp.idm.service.IdentityService;

/**
 * Базовая реализация сервиса кадровых изменений.
 * Соответствует схеме алгоритма подпрограммы обработки кадровых событий (рис. 8.2).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class IdentityServiceImpl implements IdentityService {

	private final UserRepository userRepository;
	private final SecurityLogRepository securityLogRepository;

	/**
	 * Основная подпрограмма обработки кадрового события (согласно рис. 8.2).
	 * Маршрутизирует событие в нужный обработчик (Joiner/Mover/Leaver).
	 *
	 * @param eventType тип события (должен быть HR_EVENT)
	 * @param user пользователь, к которому относится событие
	 * @param details дополнительные данные события
	 * @return флаг успешности обработки
	 */
	@Override
	public boolean processLifecycleEvent(String eventType, User user, String details) {
		if (user == null) {
			log.error("Пользователь не найден для обработки кадрового события");
			writeAuditLog(null, "HR_EVENT_ERROR", "Пользователь не найден.");
			return false;
		}

		log.info("Запуск processLifecycleEvent: eventType={}, userId={}, details={}", eventType, user.getId(), details);

		String eventTypeLower = eventType.toLowerCase();
		boolean success;

		// Определение типа кадрового события и вызов соответствующей подпрограммы
		if (eventTypeLower.contains("joiner")) {
			log.info("Обработка события JOINER для пользователя {}", user.getLogin());
			processJoiner(user, details);
			success = true;
		} else if (eventTypeLower.contains("mover")) {
			log.info("Обработка события MOVER для пользователя {}", user.getLogin());
			processMover(user, details);
			success = true;
		} else if (eventTypeLower.contains("leaver")) {
			log.info("Обработка события LEAVER для пользователя {}", user.getLogin());
			success = processLeaver(user, details);
		} else {
			log.warn("Неопознанный тип кадрового события: {}", eventType);
			writeAuditLog(user, "HR_EVENT_UNKNOWN", "Неопознанный тип события: " + eventType);
			return false;
		}

		// Запись аудита после успешной обработки
		if (success) {
			String auditMessage = "Кадровое событие " + eventType + " обработано для пользователя " + user.getLogin();
			writeAuditLog(user, "HR_EVENT_SUCCESS", auditMessage);
		}

		log.info("processLifecycleEvent завершена: success={}, userId={}", success, user.getId());
		return success;
	}

	/**
	 * Подпрограмма обработки события приема сотрудника (Joiner).
	 * Устанавливает статус ACTIVE и дату приема.
	 */
	@Override
	public void processJoiner(User user, String details) {
		log.info("processJoiner: userId={}, login={}", user.getId(), user.getLogin());

		UserStatus oldStatus = user.getStatus();
		user.setStatus(UserStatus.ACTIVE);
		user.setHireDate(LocalDate.now());
		user.setTerminationDate(null);
		userRepository.save(user);

		log.info("processJoiner завершена: пользователь {} переведен из статуса {} в ACTIVE",
				user.getLogin(), oldStatus);
	}

	/**
	 * Подпрограмма обработки события перемещения сотрудника (Mover).
	 * Изменяет подразделение или другие атрибуты пользователя.
	 */
	@Override
	public void processMover(User user, String details) {
		log.info("processMover: userId={}, login={}, details={}", user.getId(), user.getLogin(), details);

		// В демо-версии подразделение не изменяется, так как требуется парсинг details
		// Реальная реализация должна извлекать department_id из details и обновлять поле
		UserStatus oldStatus = user.getStatus();
		userRepository.save(user);

		log.info("processMover завершена: пользователь {} обновлен (старый статус: {})",
				user.getLogin(), oldStatus);
	}

	/**
	 * Подпрограмма обработки события увольнения сотрудника (Leaver).
	 * Устанавливает статус TERMINATED и дату увольнения (soft delete).
	 *
	 * @return флаг успешности "мягкого" удаления
	 */
	@Override
	public boolean processLeaver(User user, String details) {
		log.info("processLeaver: userId={}, login={}", user.getId(), user.getLogin());

		boolean isSoftDeleted = false;

		try {
			UserStatus oldStatus = user.getStatus();
			user.setStatus(UserStatus.TERMINATED);
			user.setTerminationDate(LocalDate.now());
			userRepository.save(user);

			// Флаг успеха — пользователь переведен в TERMINATED
			isSoftDeleted = user.getStatus() == UserStatus.TERMINATED;

			log.info("processLeaver завершена: пользователь {} переведен из статуса {} в TERMINATED (soft delete: {})",
					user.getLogin(), oldStatus, isSoftDeleted);
		} catch (Exception e) {
			log.error("Ошибка при обработке Leaver для пользователя {}: {}", user.getLogin(), e.getMessage());
			isSoftDeleted = false;
		}

		return isSoftDeleted;
	}

	/**
	 * Запись события в журнал аудита безопасности (security_logs).
	 *
	 * @param user пользователь, к которому относится событие
	 * @param eventType тип события
	 * @param description описание события
	 */
	private void writeAuditLog(User user, String eventType, String description) {
		try {
			SecurityLog logEntry = SecurityLog.builder()
					.eventTime(LocalDateTime.now())
					.user(user)
					.eventType(eventType)
					.description(description)
					.build();
			securityLogRepository.save(logEntry);
			log.debug("Аудит записан: type={}, userId={}, desc={}", eventType,
					user != null ? user.getId() : null, description);
		} catch (Exception e) {
			log.error("Ошибка при записи в audit log: {}", e.getMessage());
		}
	}
}

