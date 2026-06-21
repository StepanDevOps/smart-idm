package ru.mtkp.idm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mtkp.idm.model.SecurityLog;
import ru.mtkp.idm.model.User;
import ru.mtkp.idm.repository.SecurityLogRepository;
import ru.mtkp.idm.service.AuditService;

import java.time.LocalDateTime;

/**
 * Реализация сервиса аудита безопасности.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuditServiceImpl implements AuditService {

    private final SecurityLogRepository securityLogRepository;

    @Override
    public void logAction(User user, String eventType, String description) {
        try {
            SecurityLog logEntry = SecurityLog.builder()
                    .eventTime(LocalDateTime.now())
                    .user(user)
                    .eventType(eventType)
                    .description(description)
                    .build();

            securityLogRepository.save(logEntry);

            log.debug("Аудит записан: type={}, userId={}, desc={}",
                    eventType,
                    user != null ? user.getId() : null,
                    description);

        } catch (Exception e) {
            log.error("Ошибка при записи в audit log: {}", e.getMessage(), e);
        }
    }
}
