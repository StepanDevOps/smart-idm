package ru.mtkp.idm.validator;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.mtkp.idm.model.Department;
import ru.mtkp.idm.model.User;
import ru.mtkp.idm.model.UserStatus;
import ru.mtkp.idm.repository.DepartmentRepository;

/**
 * Валидатор для назначений ролей.
 * Проверяет корректность данных перед созданием или обновлением назначения.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleAssignmentValidator {

    private final DepartmentRepository departmentRepository;

	/**
	 * Проверяет, что effectiveTo >= effectiveFrom.
	 *
	 * @param effectiveFrom дата начала
	 * @param effectiveTo дата окончания
	 * @return true если валидно
	 * @throws IllegalArgumentException если не валидно
	 */
	public boolean validateDateRange(LocalDate effectiveFrom, LocalDate effectiveTo) {
		if (effectiveFrom == null) {
			log.warn("effectiveFrom не указан");
			return false;
		}
		if (effectiveTo != null && !effectiveTo.isAfter(effectiveFrom)) {
			log.warn("effectiveTo ({}) должно быть после effectiveFrom ({})", effectiveTo, effectiveFrom);
			return false;
		}
		return true;
	}

	/**
	 * Проверяет, что пользователь не уволен.
	 *
	 * @param user пользователь
	 * @return true если валидно
	 * @throws IllegalArgumentException если пользователь уволен
	 */
	public boolean validateUserNotTerminated(User user) {
		if (user.getStatus() == UserStatus.TERMINATED) {
			log.warn("Нельзя назначить роль уволенному пользователю: {}", user.getLogin());
			throw new IllegalArgumentException("Нельзя назначить роль уволенному пользователю: " + user.getLogin());
		}
		return true;
	}

	/**
	 * Проверяет, что департамент существует.
	 *
	 * @param departmentId идентификатор департамента
	 * @throws IllegalArgumentException если департамент не найден
	 */
	public void validateDepartmentExists(Integer departmentId) {
		if (departmentId == null) {
			return; // INDIRECT может быть null, это не ошибка
		}
		Department department = departmentRepository.findById(departmentId).orElse(null);
		if (department == null) {
			throw new IllegalArgumentException("Департамент не найден: " + departmentId);
		}
	}

	/**
	 * Проверяет все валидации для назначения.
	 *
	 * @param user пользователь
	 * @param effectiveFrom дата начала
	 * @param effectiveTo дата окончания
	 * @param departmentId идентификатор департамента (для INDIRECT)
	 * @throws IllegalArgumentException если валидация не пройдена
	 */
	public void validateAll(User user, LocalDate effectiveFrom, LocalDate effectiveTo, Integer departmentId) {
		log.info("Выполнение валидации назначения: userId={}, effectiveFrom={}, effectiveTo={}, departmentId={}",
				user.getId(), effectiveFrom, effectiveTo, departmentId);

		validateUserNotTerminated(user);
		validateDateRange(effectiveFrom, effectiveTo);
		validateDepartmentExists(departmentId);

		// Проверка: если есть departmentId, то это INDIRECT назначение
		if (departmentId != null) {
			log.info("INDIRECT назначение через департамент: {}", departmentId);
		}

		log.info("Валидация прошла успешно для пользователя: {}", user.getLogin());
	}
}
