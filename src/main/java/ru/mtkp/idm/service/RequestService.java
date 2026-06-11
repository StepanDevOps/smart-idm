package ru.mtkp.idm.service;

import ru.mtkp.idm.model.Request;
import ru.mtkp.idm.model.RequestStatus;

/**
 * Сервис управления жизненным циклом заявок и этапов согласования.
 * Содержит подпрограмму resolveRequestStep согласно схеме алгоритма (рис. 8.3).
 */
public interface RequestService {

	/**
	 * Обрабатывает входящее событие доступа (ACCESS_REQUEST).
	 * Создаёт заявку и запускает workflow согласования.
	 *
	 * @param requestor инициатор заявки
	 * @param requestedFor пользователь, для которого запрашивается доступ
	 * @param roleName имя запрашиваемой роли
	 * @param targetSystem целевая система
	 * @param justification обоснование запроса
	 * @return созданная заявка
	 */
	Request createAccessRequest(ru.mtkp.idm.model.User requestor, ru.mtkp.idm.model.User requestedFor, String roleName,
								String targetSystem, String justification);

	/**
	 * Подпрограмма согласования и provisioning (согласно рис. 8.3).
	 * Обрабатывает решение на текущем этапе согласования и запускает provisioning при необходимости.
	 *
	 * @param requestId идентификатор заявки
	 * @param approverId идентификатор утверждающего пользователя
	 * @param approved флаг решения (true = одобрено, false = отклонено)
	 * @return флаг успешности обработки
	 */
	boolean resolveRequestStep(Integer requestId, Long approverId, boolean approved);

	/**
	 * Переходит к следующему этапу согласования или завершает заявку.
	 *
	 * @param request заявка
	 * @return следующий статус заявки
	 */
	RequestStatus proceedToNextStep(Request request);
}
