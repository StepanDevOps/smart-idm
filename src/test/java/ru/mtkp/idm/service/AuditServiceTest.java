package ru.mtkp.idm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mtkp.idm.model.SecurityLog;
import ru.mtkp.idm.model.User;
import ru.mtkp.idm.model.UserStatus;
import ru.mtkp.idm.repository.SecurityLogRepository;
import ru.mtkp.idm.service.impl.AuditServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для AuditServiceImpl.
 * Проверяют логирование событий безопасности.
 */
@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private SecurityLogRepository securityLogRepository;

    @InjectMocks
    private AuditServiceImpl auditService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .login("testuser")
                .firstName("Иван")
                .lastName("Иванов")
                .emailWork("ivanov@test.ru")
                .hireDate(LocalDate.now())
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void testLogAction_SavesSecurityLog() {
        // Arrange
        String eventType = "TEST_EVENT";
        String description = "Тестовое событие";

        // Act
        auditService.logAction(testUser, eventType, description);

        // Assert
        ArgumentCaptor<SecurityLog> captor = ArgumentCaptor.forClass(SecurityLog.class);
        verify(securityLogRepository, times(1)).save(captor.capture());

        SecurityLog savedLog = captor.getValue();
        assertEquals(testUser, savedLog.getUser());
        assertEquals(eventType, savedLog.getEventType());
        assertEquals(description, savedLog.getDescription());
        assertNotNull(savedLog.getEventTime());
    }

    @Test
    void testLogAction_NullUser_SystemEvent() {
        // Arrange
        String eventType = "SYSTEM_EVENT";
        String description = "Системное событие без пользователя";

        // Act
        auditService.logAction(null, eventType, description);

        // Assert
        ArgumentCaptor<SecurityLog> captor = ArgumentCaptor.forClass(SecurityLog.class);
        verify(securityLogRepository, times(1)).save(captor.capture());

        SecurityLog savedLog = captor.getValue();
        assertNull(savedLog.getUser());
        assertEquals(eventType, savedLog.getEventType());
    }

    @Test
    void testLogAction_ExceptionHandling_DoesNotThrow() {
        // Arrange
        doThrow(new RuntimeException("DB error"))
                .when(securityLogRepository).save(any(SecurityLog.class));

        // Act & Assert
        assertDoesNotThrow(() -> {
            auditService.logAction(testUser, "ERROR_EVENT", "Ошибка БД");
        });
    }

    @Test
    void testLogAction_EventTimeIsSet() {
        // Arrange
        LocalDateTime before = LocalDateTime.now();

        // Act
        auditService.logAction(testUser, "TIME_TEST", "Проверка времени");

        // Assert
        ArgumentCaptor<SecurityLog> captor = ArgumentCaptor.forClass(SecurityLog.class);
        verify(securityLogRepository, times(1)).save(captor.capture());

        SecurityLog savedLog = captor.getValue();
        assertTrue(savedLog.getEventTime().isAfter(before.minusSeconds(1)));
        assertTrue(savedLog.getEventTime().isBefore(before.plusSeconds(1)));
    }

    @Test
    void testLogAction_MultipleEvents() {
        // Arrange
        int eventCount = 5;

        // Act
        for (int i = 0; i < eventCount; i++) {
            auditService.logAction(testUser, "EVENT_" + i, "Событие #" + i);
        }

        // Assert
        verify(securityLogRepository, times(eventCount)).save(any(SecurityLog.class));
    }
}
