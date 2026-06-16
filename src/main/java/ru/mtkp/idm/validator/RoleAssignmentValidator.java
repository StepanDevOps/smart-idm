package ru.mtkp.idm.validator;

import java.time.LocalDate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.mtkp.idm.model.User;
import ru.mtkp.idm.model.UserStatus;

/**
 * Валидатор для назначений ролей.
 * Проверяет корректность данных перед созданием или обновлением назначения.
 */
@Slf4j
@Component
public class RoleAssignmentValidator {

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
	 * Проверяет все валидации для назначения.
	 *
	 * @param user пользователь
	 * @param effectiveFrom дата начала
	 * @param effectiveTo дата окончания
	 * @throws IllegalArgumentException если валидация не пройдена
	 */
	public void validateAll(User user, LocalDate effectiveFrom, LocalDate effectiveTo) {
		log.info("Выполнение валидации назначения: userId={}, effectiveFrom={}, effectiveTo={}",
				user.getId(), effectiveFrom, effectiveTo);

		validateUserNotTerminated(user);
		validateDateRange(effectiveFrom, effectiveTo);

		log.info("Валидация прошла успешно для пользователя: {}", user.getLogin());
	}
}
