package ru.mtkp.idm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mtkp.idm.model.Department;
import ru.mtkp.idm.model.User;
import ru.mtkp.idm.model.UserStatus;
import ru.mtkp.idm.repository.DepartmentRepository;
import ru.mtkp.idm.repository.UserRepository;
import ru.mtkp.idm.service.impl.IdentityServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для IdentityServiceImpl.
 * Проверяют обработку HR-событий (Joiner/Mover/Leaver).
 */
@ExtendWith(MockitoExtension.class)
class IdentityServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentRoleService departmentRoleService;

    @Mock
    private RoleAssignmentService roleAssignmentService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private IdentityServiceImpl identityService;

    private User testUser;
    private Department testDepartment;

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

        testDepartment = new Department();
        testDepartment.setId(1);
        testDepartment.setName("Тестовый отдел");
        testDepartment.setCode("TEST-001");

        testUser.setDepartment(testDepartment);
    }

    @Test
    void testProcessJoiner_SetsActiveStatus() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(departmentRoleService.getAllRolesForDepartmentWithChildren(1))
                .thenReturn(List.of()); // Нет INDIRECT ролей

        // Act
        identityService.processJoiner(testUser, "Приём на работу");

        // Assert
        assertEquals(UserStatus.ACTIVE, testUser.getStatus());
        assertNotNull(testUser.getHireDate());
        assertNull(testUser.getTerminationDate());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testProcessJoiner_AssignsIndirectRoles() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(departmentRoleService.getAllRolesForDepartmentWithChildren(1))
                .thenReturn(List.of(100, 200)); // Две INDIRECT роли
        when(roleAssignmentService.getAssignmentsByUserAndRole(1L, 100)).thenReturn(List.of());
        when(roleAssignmentService.getAssignmentsByUserAndRole(1L, 200)).thenReturn(List.of());

        // Act
        identityService.processJoiner(testUser, "Приём на работу");

        // Assert
        verify(roleAssignmentService, times(2)).createIndirectAssignment(anyLong(), anyInt(), anyInt(), anyString());
    }

    @Test
    void testProcessLeaver_SetsTerminatedStatus() {
        // Arrange
        testUser.setStatus(UserStatus.ACTIVE);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        boolean result = identityService.processLeaver(testUser, "Увольнение");

        // Assert
        assertTrue(result);
        assertEquals(UserStatus.TERMINATED, testUser.getStatus());
        assertNotNull(testUser.getTerminationDate());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testProcessLifecycleEvent_UnknownEvent_ReturnsFalse() {
        // Arrange & Act
        boolean result = identityService.processLifecycleEvent("UNKNOWN_EVENT", testUser, "Тест");

        // Assert
        assertFalse(result);
        verify(auditService, times(1)).logAction(eq(testUser), eq("HR_EVENT_UNKNOWN"), anyString());
    }

    @Test
    void testProcessLifecycleEvent_NullUser_ReturnsFalse() {
        // Arrange & Act
        boolean result = identityService.processLifecycleEvent("HR_EVENT_JOINER", null, "Тест");

        // Assert
        assertFalse(result);
        verify(auditService, times(1)).logAction(eq(null), eq("HR_EVENT_ERROR"), anyString());
    }
}
