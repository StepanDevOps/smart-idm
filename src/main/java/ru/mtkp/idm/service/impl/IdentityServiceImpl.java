package ru.mtkp.idm.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.mtkp.idm.model.User;
import ru.mtkp.idm.service.IdentityService;

/**
 * Базовая реализация сервиса кадровых изменений.
 */
@Slf4j
@Service
public class IdentityServiceImpl implements IdentityService {

	/**
	 * Обрабатывает событие приема сотрудника.
	 *
	 * @param user пользователь
	 * @param details детали события
	 */
	@Override
	public void processJoiner(User user, String details) {
		log.info("Обработка Joiner выполнена для пользователя {}", user.getLogin());
	}

	/**
	 * Обрабатывает событие перемещения сотрудника.
	 *
	 * @param user пользователь
	 * @param details детали события
	 */
	@Override
	public void processMover(User user, String details) {
		log.info("Обработка Mover выполнена для пользователя {}", user.getLogin());
	}

	/**
	 * Обрабатывает событие увольнения сотрудника.
	 *
	 * @param user пользователь
	 * @param details детали события
	 */
	@Override
	public void processLeaver(User user, String details) {
		log.info("Обработка Leaver выполнена для пользователя {}", user.getLogin());
	}
}

