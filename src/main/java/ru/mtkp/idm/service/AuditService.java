package ru.mtkp.idm.service;

import ru.mtkp.idm.model.User;

/**
 * Сервис аудита безопасности.
 * Предоставляет единый интерфейс для логирования событий безопасности.
 */
public interface AuditService {

    /**
     * Запись события в журнал аудита безопасности.
     *
     * @param user        пользователь, совершивший действие (может быть null для системных событий)
     * @param eventType   тип события (например, "HR_EVENT_CREATED", "ROLE_ASSIGNMENT_REVOKED")
     * @param description описание события
     */
    void logAction(User user, String eventType, String description);
}
