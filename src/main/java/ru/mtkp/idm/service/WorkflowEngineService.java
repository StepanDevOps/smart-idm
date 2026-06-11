package ru.mtkp.idm.service;

import java.util.Locale;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.mtkp.idm.model.EventType;
import ru.mtkp.idm.model.Request;
import ru.mtkp.idm.model.User;
import ru.mtkp.idm.repository.UserRepository;

/**
 * Оркестратор бизнес-процесса IDM.
 * Основная точка входа для обработки всех IDM-событий.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowEngineService {

	private final IdentityService identityService;
	private final ProvisioningService provisioningService;
	private final RequestService requestService;
	private final UserRepository userRepository;

	/**
	 * Обрабатывает входящее IDM-событие и маршрутизирует его в нужный сервис.
	 *
	 * @param eventType тип события (HR_EVENT или ACCESS_REQUEST)
	 * @param userId идентификатор пользователя
	 * @param details дополнительные сведения
	 * @return результат обработки (успех/неудача)
	 */
	public boolean handleIdmEvent(String eventType, Long userId, String details) {
		log.info("Получено IDM-событие: тип={}, userId={}, details={}", eventType, userId, details);

		EventType type = parseEventType(eventType);
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));

		boolean result = switch (type) {
			case HR_EVENT -> handleHrEvent(user, details);
			case ACCESS_REQUEST -> handleAccessRequest(user, details);
		};

		log.info("IDM-событие обработано: тип={}, userId={}, result={}", eventType, userId, result);
		return result;
	}

	/**
	 * Обработка HR-события (Joiner/Mover/Leaver).
	 * Вызывает подпрограмму processLifecycleEvent из IdentityService (рис. 8.2).
	 */
	private boolean handleHrEvent(User user, String details) {
		log.info("Запуск обработки HR-события для пользователя {}", user.getLogin());
		return identityService.processLifecycleEvent(details, user, details);
	}

	/**
	 * Обработка события запроса доступа.
	 * Создаёт заявку и запускает workflow согласования (рис. 8.3).
	 */
	private boolean handleAccessRequest(User user, String details) {
		log.info("Запуск обработки Access Request для пользователя {}", user.getLogin());

		// В демо-версии используем заглушки для roleName и targetSystem
		// В реальной системе эти данные извлекаются из details или формы заявки
		String roleName = extractRoleName(details);
		String targetSystem = extractTargetSystem(details);
		String justification = extractJustification(details);

		Request request = requestService.createAccessRequest(
				user,  // requestor
				user,  // requestedFor (в демо — сам себе запрашивает)
				roleName,
				targetSystem,
				justification
		);

		log.info("Заявка на доступ создана: id={}, status={}", request.getId(), request.getStatus());
		return true;
	}

	/**
	 * Парсинг типа события.
	 */
	private EventType parseEventType(String eventType) {
		if (eventType == null || eventType.isBlank()) {
			throw new IllegalArgumentException("Тип события не задан");
		}

		try {
			return EventType.valueOf(eventType.trim().toUpperCase(Locale.ROOT));
		} catch (Exception ex) {
			throw new IllegalArgumentException("Неверный тип события: " + eventType, ex);
		}
	}

	// ==================== Вспомогательные методы для извлечения данных ====================

	private String extractTargetSystem(String details) {
		// В демо-версии — заглушка
		return "DEFAULT_SYSTEM";
	}

	private String extractRoleName(String details) {
		// В демо-версии — заглушка
		return "DEFAULT_ROLE";
	}

	private String extractJustification(String details) {
		// В демо-версии используем details как обоснование
		return details != null ? details : "Запрос доступа";
	}
}

