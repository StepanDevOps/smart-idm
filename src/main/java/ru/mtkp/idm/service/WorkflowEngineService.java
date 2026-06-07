package ru.mtkp.idm.service;

import java.util.Locale;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.mtkp.idm.model.EventType;
import ru.mtkp.idm.model.IdmRequest;
import ru.mtkp.idm.model.RequestStatus;
import ru.mtkp.idm.model.User;
import ru.mtkp.idm.repository.IdmRequestRepository;
import ru.mtkp.idm.repository.UserRepository;

/**
 * Оркестратор бизнес-процесса IDM.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowEngineService {

	@Autowired
	private IdentityService identityService;

	@Autowired
	private ProvisioningService provisioningService;
	private final UserRepository userRepository;
	private final IdmRequestRepository idmRequestRepository;

	/**
	 * Обрабатывает входящее IDM-событие и маршрутизирует его в нужный сервис.
	 *
	 * @param eventType тип события
	 * @param userId идентификатор пользователя
	 * @param details дополнительные сведения
	 */
	public void handleIdmEvent(String eventType, Long userId, String details) {
		log.info("Получено IDM-событие: тип={}, userId={}", eventType, userId);

		var type = parseEventType(eventType);
		var user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));

		var request = IdmRequest.builder()
				.user(user)
				.status(RequestStatus.CREATED)
				.targetSystem(extractTargetSystem(details))
				.roleName(extractRoleName(details))
				.build();
		request = idmRequestRepository.save(request);

		switch (type) {
			case HR_EVENT -> handleHrEvent(user, details, request);
			case ACCESS_REQUEST -> handleAccessRequest(user, details, request);
			default -> throw new IllegalStateException("Неподдерживаемый тип события: " + type);
		}

		request.setStatus(RequestStatus.COMPLETED);
		idmRequestRepository.save(request);
		log.info("IDM-событие обработано успешно, requestId={}", request.getId());
	}

	private void handleHrEvent(User user, String details, IdmRequest request) {
		request.setStatus(RequestStatus.IN_PROGRESS);
		idmRequestRepository.save(request);

		if (isJoiner(details)) {
			log.info("Запущена обработка Joiner для пользователя {}", user.getUsername());
			identityService.processJoiner(user, details);
			provisioningService.createAccount(user, request.getTargetSystem(), request.getRoleName());
			return;
		}

		if (isMover(details)) {
			log.info("Запущена обработка Mover для пользователя {}", user.getUsername());
			identityService.processMover(user, details);
			provisioningService.createAccount(user, request.getTargetSystem(), request.getRoleName());
			return;
		}

		if (isLeaver(details)) {
			log.info("Запущена обработка Leaver для пользователя {}", user.getUsername());
			identityService.processLeaver(user, details);
			provisioningService.blockAccount(user, request.getTargetSystem());
			return;
		}

		log.info("HR-событие не распознано, заявка будет помечена как завершенная без действий");
	}

	private void handleAccessRequest(User user, String details, IdmRequest request) {
		request.setStatus(RequestStatus.IN_PROGRESS);
		idmRequestRepository.save(request);

		log.info("Запущена обработка Access Request для пользователя {}", user.getUsername());
		provisioningService.createAccount(user, request.getTargetSystem(), request.getRoleName());
	}

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

	private boolean isJoiner(String details) {
		return containsToken(details, "joiner");
	}

	private boolean isMover(String details) {
		return containsToken(details, "mover");
	}

	private boolean isLeaver(String details) {
		return containsToken(details, "leaver");
	}

	private boolean containsToken(String details, String token) {
		return details != null && details.toLowerCase(Locale.ROOT).contains(token);
	}

	private String extractTargetSystem(String details) {
		return "DEFAULT_SYSTEM";
	}

	private String extractRoleName(String details) {
		return "DEFAULT_ROLE";
	}
}

