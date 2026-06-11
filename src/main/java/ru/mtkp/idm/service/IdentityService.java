package ru.mtkp.idm.service;

import ru.mtkp.idm.model.User;

/**
 * Сервис обработки кадровых изменений.
 * Содержит подпрограмму processLifecycleEvent согласно схеме алгоритма (рис. 8.2).
 */
public interface IdentityService {

	/**
	 * Обрабатывает входящее кадровое событие (Joiner/Mover/Leaver).
	 * Это основная точка входа для HR-событий согласно схеме алгоритма (рис. 8.2).
	 *
	 * @param eventType тип события (HR_EVENT)
	 * @param user пользователь, к которому относится событие
	 * @param details дополнительные данные события
	 * @return флаг успешности обработки
	 */
	boolean processLifecycleEvent(String eventType, User user, String details);

	/**
	 * Обрабатывает событие приема сотрудника (Joiner).
	 *
	 * @param user пользователь
	 * @param details детали события
	 */
	void processJoiner(User user, String details);

	/**
	 * Обрабатывает событие перемещения сотрудника (Mover).
	 *
	 * @param user пользователь
	 * @param details детали события
	 */
	void processMover(User user, String details);

	/**
	 * Обрабатывает событие увольнения сотрудника (Leaver).
	 *
	 * @param user пользователь
	 * @param details детали события
	 * @return флаг успешности "мягкого" удаления (soft delete)
	 */
	boolean processLeaver(User user, String details);
}

