package ru.mtkp.idm.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.mtkp.idm.model.Role;
import ru.mtkp.idm.model.RoleAssignment;
import ru.mtkp.idm.model.User;
import ru.mtkp.idm.model.UserStatus;
import ru.mtkp.idm.model.AssignType;
import ru.mtkp.idm.repository.RoleAssignmentRepository;
import ru.mtkp.idm.repository.RoleRepository;
import ru.mtkp.idm.repository.UserRepository;
import ru.mtkp.idm.service.RoleAssignmentService;

/**
 * Реализация сервиса управления назначениями ролей.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoleAssignmentServiceImpl implements RoleAssignmentService {

	private final RoleAssignmentRepository roleAssignmentRepository;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;

	/**
	 * Назначает роль пользователю.
	 */
	@Override
	public RoleAssignment assignRoleToUser(Long userId, Integer roleId, String reason,
										   LocalDate effectiveFrom, LocalDate effectiveTo) {
		log.info("Назначение роли: userId={}, roleId={}, reason={}, effectiveFrom={}, effectiveTo={}",
				userId, roleId, reason, effectiveFrom, effectiveTo);

		// Валидация: пользователь существует
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));

		// Валидация: пользователь не уволен
		if (user.getStatus() == UserStatus.TERMINATED) {
			throw new IllegalStateException("Нельзя назначить роль уволенному пользователю: " + user.getLogin());
		}

		// Валидация: роль существует
		Role role = roleRepository.findById(roleId)
				.orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleId));

		// Валидация: даты
		if (effectiveFrom == null) {
			effectiveFrom = LocalDate.now();
		}
		if (effectiveTo != null && !effectiveTo.isAfter(effectiveFrom)) {
			throw new IllegalArgumentException("effectiveTo должно быть после effectiveFrom");
		}

		// Проверка: нет ли уже активного назначения этой роли
		List<RoleAssignment> existingAssignments = roleAssignmentRepository.findByUserId(userId);
		boolean hasActiveAssignment = existingAssignments.stream()
				.anyMatch(a -> a.getRole().getId().equals(roleId) && a.getEffectiveTo() == null);

		if (hasActiveAssignment) {
			log.warn("У пользователя {} уже есть активное назначение роли {}", user.getLogin(), role.getName());
			// В реальном проекте можно выбросить исключение или обновить существующее
		}

		// Создаём назначение
		RoleAssignment assignment = RoleAssignment.builder()
				.user(user)
				.role(role)
				.assignmentType(AssignType.DIRECT)
				.effectiveFrom(effectiveFrom)
				.effectiveTo(effectiveTo)
				.assignmentReason(reason)
				.build();

		RoleAssignment saved = roleAssignmentRepository.save(assignment);
		log.info("Роль успешно назначена: assignmentId={}, userId={}, roleId={}",
				saved.getId(), userId, roleId);

		return saved;
	}

	/**
	 * Отзывает назначение роли.
	 */
	@Override
	public boolean revokeRoleAssignment(Integer assignmentId) {
		log.info("Отзыв назначения роли: assignmentId={}", assignmentId);

		RoleAssignment assignment = roleAssignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new IllegalArgumentException("Назначение не найдено: " + assignmentId));

		// Устанавливаем effectiveTo на вчерашний день
		LocalDate expiryDate = LocalDate.now().minusDays(1);

		// Если уже истёкший, не трогаем
		if (assignment.getEffectiveTo() != null && assignment.getEffectiveTo().isBefore(LocalDate.now())) {
			log.warn("Назначение уже истёкшее: assignmentId={}, effectiveTo={}", assignmentId, assignment.getEffectiveTo());
			return false;
		}

		assignment.setEffectiveTo(expiryDate);
		roleAssignmentRepository.save(assignment);

		log.info("Назначение отозвано: assignmentId={}, effectiveTo={}", assignmentId, expiryDate);
		return true;
	}

	/**
	 * Получает все активные назначения ролей для пользователя.
	 */
	@Override
	@Transactional(readOnly = true)
	public List<RoleAssignment> getActiveAssignments(Long userId) {
		log.info("Получение активных назначений для пользователя: userId={}", userId);
		return roleAssignmentRepository.findByUserIdAndEffectiveToIsNull(userId);
	}

	/**
	 * Получает все истёкшие назначения ролей.
	 */
	@Override
	@Transactional(readOnly = true)
	public List<RoleAssignment> getExpiredAssignments() {
		log.info("Получение истёкших назначений ролей");
		return roleAssignmentRepository.findExpiredAssignments(LocalDate.now());
	}

	/**
	 * Получает все назначения ролей для пользователя.
	 */
	@Override
	@Transactional(readOnly = true)
	public List<RoleAssignment> getAllAssignments(Long userId) {
		log.info("Получение всех назначений для пользователя: userId={}", userId);
		return roleAssignmentRepository.findByUserId(userId);
	}

	/**
	 * Получает назначение по идентификатору.
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<RoleAssignment> getAssignmentById(Integer assignmentId) {
		return roleAssignmentRepository.findByIdWithUserAndRole(assignmentId);
	}

	/**
	 * Проверяет, имеет ли пользователь роль.
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean hasRole(Long userId, Integer roleId) {
		List<RoleAssignment> assignments = roleAssignmentRepository.findByUserId(userId);
		return assignments.stream()
				.anyMatch(a -> a.getRole().getId().equals(roleId)
						&& (a.getEffectiveTo() == null || a.getEffectiveTo().isAfter(LocalDate.now())));
	}

	/**
	 * Получает всех пользователей с определённой ролью.
	 */
	@Override
	@Transactional(readOnly = true)
	public List<RoleAssignment> getUsersWithRole(Integer roleId) {
		log.info("Получение пользователей с ролью: roleId={}", roleId);
		return roleAssignmentRepository.findByRoleIdAndEffectiveToIsNull(roleId);
	}

	/**
	 * Получает все назначения ролей для всех пользователей.
	 */
	@Override
	@Transactional(readOnly = true)
	public List<RoleAssignment> getAllAssignmentsForAllUsers() {
		log.info("Получение всех назначений ролей");
		return roleAssignmentRepository.findAllWithUserAndRole();
	}
}
