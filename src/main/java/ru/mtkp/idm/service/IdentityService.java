package ru.mtkp.idm.service;

import ru.mtkp.idm.model.User;

/**
 * Сервис обработки кадровых изменений.
 */
public interface IdentityService {

	/**
	 * Обрабатывает событие приема сотрудника.
	 *
	 * @param user пользователь
	 * @param details детали события
	 */
	void processJoiner(User user, String details);

	/**
	 * Обрабатывает событие перемещения сотрудника.
	 *
	 * @param user пользователь
	 * @param details детали события
	 */
	void processMover(User user, String details);

	/**
	 * Обрабатывает событие увольнения сотрудника.
	 *
	 * @param user пользователь
	 * @param details детали события
	 */
	void processLeaver(User user, String details);
}

