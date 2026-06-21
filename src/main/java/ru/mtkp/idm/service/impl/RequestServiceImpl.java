package ru.mtkp.idm.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.mtkp.idm.model.AppDecision;
import ru.mtkp.idm.model.ApprovalStep;
import ru.mtkp.idm.model.Request;
import ru.mtkp.idm.model.RequestStatus;
import ru.mtkp.idm.model.Role;
import ru.mtkp.idm.model.StepName;
import ru.mtkp.idm.model.User;
import ru.mtkp.idm.repository.ApprovalStepRepository;
import ru.mtkp.idm.repository.RequestRepository;
import ru.mtkp.idm.repository.RoleRepository;
import ru.mtkp.idm.repository.UserRepository;
import ru.mtkp.idm.service.AuditService;
import ru.mtkp.idm.service.RequestService;

/**
 * Реализация сервиса управления заявками и этапами согласования.
 * Соответствует схеме алгоритма подпрограммы согласования и provisioning (рис. 8.3).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {

	private final RequestRepository requestRepository;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final ApprovalStepRepository approvalStepRepository;
	private final ProvisioningServiceImpl provisioningService;
	private final AuditService auditService;

	/**
	 * Создаёт заявку на доступ и инициирует workflow согласования.
	 *
	 * @param requestor инициатор заявки
	 * @param requestedFor пользователь, для которого запрашивается доступ
	 * @param roleName имя запрашиваемой роли
	 * @param targetSystem целевая система
	 * @param justification обоснование запроса
	 * @return созданная заявка
	 */
	@Override
	public Request createAccessRequest(User requestor, User requestedFor, String roleName,
									   String targetSystem, String justification) {
		log.info("Создание заявки на доступ: requestor={}, requestedFor={}, role={}, system={}",
				requestor.getLogin(), requestedFor.getLogin(), roleName, targetSystem);

		// Поиск или создание роли
		Role role = roleRepository.findByName(roleName)
				.orElseGet(() -> Role.builder()
						.name(roleName)
						.description("Роль, созданная автоматически: " + roleName)
						.build());
		role = roleRepository.save(role);

		// Создание заявки
		Request request = Request.builder()
				.requestor(requestor)
				.requestedFor(requestedFor)
				.role(role)
				.status(RequestStatus.CREATED)
				.justification(justification)
				.createdAt(LocalDateTime.now())
				.build();
		request = requestRepository.save(request);

		// Создание первого этапа согласования (линейный менеджер заявителя)
		User lineManager = requestedFor.getManager();
		createApprovalStep(request, lineManager, StepName.LINE_MANAGER, AppDecision.PENDING);

		// Запись в аудит
		auditService.logAction(requestor, "ACCESS_REQUEST_CREATED",
				"Создана заявка #" + request.getId() + " на роль " + roleName);

		log.info("Заявка создана: id={}, status={}", request.getId(), request.getStatus());
		return request;
	}

	/**
	 * Подпрограмма согласования и provisioning (согласно рис. 8.3).
	 *
	 * Алгоритм:
	 * 1. Найти заявку по ID
	 * 2. Найти текущего утверждающего на активном этапе
	 * 3. Проверить права утверждающего
	 * 4. Обновить решение на текущем этапе
	 * 5. Если отклонено -> статус REJECTED, записать лог
	 * 6. Если одобрено -> перейти к следующему этапу или выполнить provisioning
	 * 7. Вернуть результат
	 *
	 * @param requestId идентификатор заявки
	 * @param approverId идентификатор утверждающего пользователя
	 * @param approved флаг решения (true = одобрено, false = отклонено)
	 * @return флаг успешности обработки
	 */
	@Override
	public boolean resolveRequestStep(Integer requestId, Long approverId, boolean approved) {
		log.info("resolveRequestStep: requestId={}, approverId={}, approved={}",
				requestId, approverId, approved);

		// Шаг 1: Найти заявку
		Request request = requestRepository.findById(requestId)
				.orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + requestId));

		User approver = userRepository.findById(approverId)
				.orElseThrow(() -> new IllegalArgumentException("Утверждающий не найден: " + approverId));

		// Шаг 2: Найти текущий активный этап согласования
		Optional<ApprovalStep> currentStepOpt = findActiveApprovalStep(request);
		if (currentStepOpt.isEmpty()) {
			log.warn("Активный этап согласования не найден для заявки {}", requestId);
			auditService.logAction(approver, "REQUEST_STEP_ERROR",
					"Активный этап не найден для заявки #" + requestId);
			return false;
		}

		ApprovalStep currentStep = currentStepOpt.get();

		// Шаг 3: Проверить права утверждающего (упрощённая проверка)
		if (!canApprove(currentStep, approver)) {
			log.warn("Пользователь {} не имеет прав для утверждения заявки {} на этапе {}",
					approver.getLogin(), requestId, currentStep.getStepName());
			auditService.logAction(approver, "REQUEST_STEP_DENIED",
					"Отказ в правах на утверждение заявки #" + requestId);
			return false;
		}

		// Шаг 4: Обновить решение на текущем этапе
		AppDecision decision = approved ? AppDecision.APPROVED : AppDecision.REJECTED;
		currentStep.setDecision(decision);
		currentStep.setDecidedAt(LocalDateTime.now());
		currentStep.setApprover(approver);
		approvalStepRepository.save(currentStep);

		log.info("Этап {} одобрен: requestId={}, approver={}, decision={}",
				currentStep.getStepName(), requestId, approver.getLogin(), decision);

		// Шаг 5: Обработать решение
		if (!approved) {
			// Отклонение заявки
			request.setStatus(RequestStatus.REJECTED);
			request.setResolvedAt(LocalDateTime.now());
			requestRepository.save(request);

			auditService.logAction(approver, "REQUEST_REJECTED",
					"Заявка #" + requestId + " отклонена на этапе " + currentStep.getStepName());
			log.info("Заявка #{} отклонена", requestId);
			return true;
		}

		// Шаг 6: Одобрение - переход к следующему этапу или provisioning
		RequestStatus nextStatus = proceedToNextStep(request);
		request.setStatus(nextStatus);

		if (nextStatus == RequestStatus.COMPLETED) {
			// Все этапы пройдены - выполнить provisioning
			log.info("Все этапы согласования пройдены, запуск provisioning для заявки #{}", requestId);
			executeProvisioning(request);
			request.setResolvedAt(LocalDateTime.now());
			auditService.logAction(approver, "REQUEST_COMPLETED",
					"Заявка #" + requestId + " завершена, provisioning выполнен");
		} else {
			auditService.logAction(approver, "REQUEST_STEP_APPROVED",
					"Заявка #" + requestId + " одобрена на этапе " + currentStep.getStepName() +
							", переход к этапу " + nextStatus);
		}

		requestRepository.save(request);
		log.info("resolveRequestStep завершена: requestId={}, nextStatus={}", requestId, nextStatus);
		return true;
	}

	/**
	 * Переходит к следующему этапу согласования или завершает заявку.
	 *
	 * Алгоритм:
	 * 1. Получить все этапы согласования заявки
	 * 2. Найти первый непросмотренный этап
	 * 3. Если такой этап есть -> создать его и вернуть APPROVED
	 * 4. Если этапов нет -> вернуть COMPLETED
	 *
	 * @param request заявка
	 * @return следующий статус заявки
	 */
	@Override
	public RequestStatus proceedToNextStep(Request request) {
		log.debug("proceedToNextStep: requestId={}", request.getId());

		List<ApprovalStep> allSteps = approvalStepRepository.findByRequestId(request.getId());
		List<ApprovalStep> pendingSteps = allSteps.stream()
				.filter(step -> step.getDecision() == AppDecision.PENDING)
				.toList();

		if (pendingSteps.isEmpty()) {
			// Все этапы пройдены
			log.info("Все этапы согласования пройдены для заявки #{}", request.getId());
			return RequestStatus.COMPLETED;
		}

		// Найти следующий этап по приоритету
		ApprovalStep nextStep = pendingSteps.stream()
				.min((s1, s2) -> Integer.compare(getStepPriority(s1.getStepName()),
						getStepPriority(s2.getStepName())))
				.orElse(pendingSteps.getFirst());

		// Если текущий этап был последним - завершаем
		if (nextStep.getStepName() == StepName.SECURITY_OFFICER) {
			log.info("Это последний этап согласования SECURITY_OFFICER для заявки #{}", request.getId());
			return RequestStatus.COMPLETED;
		}

		// Создать следующий этап
		StepName nextStepName = getNextStepName(nextStep.getStepName());
		if (nextStepName != null) {
			createApprovalStep(request, null, nextStepName, AppDecision.PENDING);
			log.info("Переход к следующему этапу: requestId={}, nextStep={}",
					request.getId(), nextStepName);
			return RequestStatus.APPROVED;
		} else {
			return RequestStatus.COMPLETED;
		}
	}

	// ==================== Вспомогательные методы ====================

	/**
	 * Найти активный (pending) этап согласования заявки.
	 */
	private Optional<ApprovalStep> findActiveApprovalStep(Request request) {
		return approvalStepRepository.findByRequestId(request.getId()).stream()
				.filter(step -> step.getDecision() == AppDecision.PENDING)
				.min((s1, s2) -> Integer.compare(getStepPriority(s1.getStepName()),
						getStepPriority(s2.getStepName())));
	}

	/**
	 * Проверить права утверждающего на текущем этапе.
	 *
	 * LINE_MANAGER: только если является менеджером requestedFor
	 * SECURITY_OFFICER: любой пользователь с ролью ADMIN
	 */
	private boolean canApprove(ApprovalStep step, User approver) {
		StepName stepName = step.getStepName();

		if (stepName == StepName.LINE_MANAGER) {
			// Линейный руководитель может согласовывать только заявки своих подчинённых
			User requestedFor = step.getRequest().getRequestedFor();
			return requestedFor.getManager() != null &&
				   requestedFor.getManager().getId().equals(approver.getId());
		} else if (stepName == StepName.SECURITY_OFFICER) {
			// Администратор ИБ - это IdmUser с ролью ADMIN
			// Упрощённая проверка: любой с ролью ADMIN (нужно добавить проверку через IdmUserRepository)
			return true; // Заглушка, пока нет проверки IdmUser.role
		}

		return false;
	}

	/**
	 * Создать этап согласования.
	 */
	private ApprovalStep createApprovalStep(Request request, User approver,
											StepName stepName, AppDecision decision) {
		ApprovalStep step = ApprovalStep.builder()
				.request(request)
				.approver(approver)
				.stepName(stepName)
				.decision(decision)
				.build();
		return approvalStepRepository.save(step);
	}

	/**
	 * Получить приоритет этапа (для определения порядка согласования).
	 */
	private int getStepPriority(StepName stepName) {
		return switch (stepName) {
			case LINE_MANAGER -> 1;
			case SECURITY_OFFICER -> 2;
		};
	}

	/**
	 * Получить следующий этап согласования.
	 */
	private StepName getNextStepName(StepName currentStep) {
		return switch (currentStep) {
			case LINE_MANAGER -> StepName.SECURITY_OFFICER;
			case SECURITY_OFFICER -> null; // Последний этап
		};
	}

	/**
	 * Выполнить provisioning после успешного согласования.
	 */
	private void executeProvisioning(Request request) {
		User user = request.getRequestedFor();
		Role role = request.getRole();
		String targetSystem = role.getSystem() != null ?
				role.getSystem().getName() : "DEFAULT_SYSTEM";
		String roleName = role.getName();

		log.info("Выполнение provisioning: userId={}, system={}, role={}",
				user.getId(), targetSystem, roleName);

		provisioningService.createAccount(user, targetSystem, roleName);
		provisioningService.completeRequest(request.getId());
	}
}
