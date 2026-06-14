package ru.mtkp.idm.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.mtkp.idm.model.Account;
import ru.mtkp.idm.model.AccountStatus;
import ru.mtkp.idm.model.ProvisioningStatus;
import ru.mtkp.idm.model.Request;
import ru.mtkp.idm.model.RequestStatus;
import ru.mtkp.idm.model.Role;
import ru.mtkp.idm.model.TargetSystem;
import ru.mtkp.idm.model.User;
import ru.mtkp.idm.model.AssignType;
import ru.mtkp.idm.model.RoleAssignment;
import ru.mtkp.idm.repository.AccountRepository;
import ru.mtkp.idm.repository.RequestRepository;
import ru.mtkp.idm.repository.RoleAssignmentRepository;
import ru.mtkp.idm.repository.RoleRepository;
import ru.mtkp.idm.repository.TargetSystemRepository;
import ru.mtkp.idm.service.ProvisioningService;

/**
 * Реализация сервиса провижининга.
 * Сохраняет учётные записи в БД и обновляет статусы заявок.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProvisioningServiceImpl implements ProvisioningService {

	private final AccountRepository accountRepository;
	private final TargetSystemRepository targetSystemRepository;
	private final RoleRepository roleRepository;
	private final RequestRepository requestRepository;
	private final RoleAssignmentRepository roleAssignmentRepository;

	/**
	 * Создаёт учётную запись в целевой системе и сохраняет в БД.
	 */
	@Override
	public void createAccount(User user, String targetSystemName, String roleName) {
		log.info("Создание учётной записи для {} в системе {} с ролью {}",
				user.getLogin(), targetSystemName, roleName);

		// Находим или создаём целевую систему
		TargetSystem system = targetSystemRepository.findByName(targetSystemName)
				.orElseGet(() -> {
					TargetSystem newSystem = TargetSystem.builder()
							.name(targetSystemName)
							.description("Система, созданная автоматически: " + targetSystemName)
							.build();
					return targetSystemRepository.save(newSystem);
				});

		// Находим или создаём роль
		Role role = roleRepository.findByName(roleName)
				.orElseGet(() -> {
					Role newRole = Role.builder()
							.name(roleName)
							.description("Роль, созданная автоматически: " + roleName)
							.system(system)
							.build();
					return roleRepository.save(newRole);
				});

		// Проверяем, нет ли уже учётной записи
		Optional<Account> existingAccount = accountRepository.findByUserIdAndSystemId(user.getId(), system.getId());
		if (existingAccount.isPresent()) {
			log.info("Учётная запись уже существует для {} в системе {}", user.getLogin(), targetSystemName);
			return;
		}

		// Создаём учётную запись
		Account account = Account.builder()
				.user(user)
				.system(system)
				.accountLogin(user.getLogin() + "@" + targetSystemName.toLowerCase())
				.status(AccountStatus.ACTIVE)
				.passwordExpiryDate(null)
				.lastLoginDate(null)
				.provisioningStatus(ProvisioningStatus.SUCCESS)
				.build();
		accountRepository.save(account);

		// Назначаем роль пользователю
		RoleAssignment assignment = RoleAssignment.builder()
				.user(user)
				.role(role)
				.assignmentType(AssignType.DIRECT)
				.effectiveFrom(java.time.LocalDate.now())
				.build();
		roleAssignmentRepository.save(assignment);

		log.info("Учётная запись создана: id={}, login={}, userId={}, systemId={}. Роль {} назначена.",
				account.getId(), account.getAccountLogin(), user.getId(), system.getId(), roleName);
	}

	/**
	 * Блокирует учётную запись в целевой системе.
	 */
	@Override
	public void blockAccount(User user, String targetSystemName) {
		log.info("Блокировка учётной записи для {} в системе {}", user.getLogin(), targetSystemName);

		TargetSystem system = targetSystemRepository.findByName(targetSystemName)
				.orElse(null);
		if (system == null) {
			log.warn("Система {} не найдена", targetSystemName);
			return;
		}

		Optional<Account> accountOpt = accountRepository.findByUserIdAndSystemId(user.getId(), system.getId());
		if (accountOpt.isPresent()) {
			Account account = accountOpt.get();
			account.setStatus(AccountStatus.DISABLED);
			account.setProvisioningStatus(ProvisioningStatus.SUCCESS);
			accountRepository.save(account);
			log.info("Учётная запись заблокирована: id={}, status={}", account.getId(), account.getStatus());
		} else {
			log.info("Учётная запись не найдена для {} в системе {}", user.getLogin(), targetSystemName);
		}
	}

	/**
	 * Завершает заявку после успешного provisioning.
	 */
	@Override
	public void completeRequest(Integer requestId) {
		log.info("Завершение заявки после provisioning: requestId={}", requestId);

		Request request = requestRepository.findById(requestId)
				.orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + requestId));

		request.setStatus(RequestStatus.COMPLETED);
		request.setResolvedAt(LocalDateTime.now());
		requestRepository.save(request);

		log.info("Заявка #{} завершена, resolvedAt={}", requestId, request.getResolvedAt());
	}
}

