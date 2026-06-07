package ru.mtkp.idm.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.mtkp.idm.model.User;
import ru.mtkp.idm.service.ProvisioningService;

/**
 * Базовая реализация сервиса интеграции с целевыми системами.
 */
@Slf4j
@Service
public class ProvisioningServiceImpl implements ProvisioningService {

	/**
	 * Создает учетную запись в целевой системе.
	 *
	 * @param user пользователь
	 * @param targetSystem целевая система
	 * @param roleName имя роли
	 */
	@Override
	public void createAccount(User user, String targetSystem, String roleName) {
		log.info("Создание учетной записи для {} в системе {} с ролью {}", user.getLogin(), targetSystem, roleName);
	}

	/**
	 * Блокирует учетную запись в целевой системе.
	 *
	 * @param user пользователь
	 * @param targetSystem целевая система
	 */
	@Override
	public void blockAccount(User user, String targetSystem) {
		log.info("Блокировка учетной записи для {} в системе {}", user.getLogin(), targetSystem);
	}
}

