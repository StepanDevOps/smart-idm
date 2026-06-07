package ru.mtkp.idm.service;

import ru.mtkp.idm.model.User;

/**
 * Сервис, имитирующий интеграцию с внешними системами.
 */
public interface ProvisioningService {

	/**
	 * Создает учетную запись в целевой системе.
	 *
	 * @param user пользователь
	 * @param targetSystem целевая система
	 * @param roleName имя роли
	 */
	void createAccount(User user, String targetSystem, String roleName);

	/**
	 * Блокирует учетную запись в целевой системе.
	 *
	 * @param user пользователь
	 * @param targetSystem целевая система
	 */
	void blockAccount(User user, String targetSystem);
}

