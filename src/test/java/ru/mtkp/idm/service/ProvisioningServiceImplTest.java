package ru.mtkp.idm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mtkp.idm.model.*;
import ru.mtkp.idm.repository.AccountRepository;
import ru.mtkp.idm.repository.RequestRepository;
import ru.mtkp.idm.repository.RoleAssignmentRepository;
import ru.mtkp.idm.repository.RoleRepository;
import ru.mtkp.idm.repository.TargetSystemRepository;
import ru.mtkp.idm.service.impl.ProvisioningServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для ProvisioningServiceImpl.
 * Проверяют создание и блокировку учётных записей в целевых системах.
 */
@ExtendWith(MockitoExtension.class)
class ProvisioningServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TargetSystemRepository targetSystemRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private RoleAssignmentRepository roleAssignmentRepository;

    @InjectMocks
    private ProvisioningServiceImpl provisioningService;

    private User testUser;
    private TargetSystem testSystem;
    private Role testRole;
    private Request testRequest;

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

        testSystem = TargetSystem.builder()
                .id(1)
                .name("TEST_SYSTEM")
                .description("Тестовая система")
                .build();

        testRole = Role.builder()
                .id(100)
                .name("TEST_ROLE")
                .description("Тестовая роль")
                .system(testSystem)
                .build();

        testRequest = Request.builder()
                .id(1)
                .requestor(testUser)
                .requestedFor(testUser)
                .role(testRole)
                .status(RequestStatus.CREATED)
                .justification("Тест")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateAccount_CreatesAccountAndRole() {
        // Arrange
        when(targetSystemRepository.findByName("TEST_SYSTEM")).thenReturn(Optional.of(testSystem));
        when(roleRepository.findByName("TEST_ROLE")).thenReturn(Optional.of(testRole));
        when(accountRepository.findByUserIdAndSystemId(1L, 1)).thenReturn(Optional.empty());

        // Act
        provisioningService.createAccount(testUser, "TEST_SYSTEM", "TEST_ROLE");

        // Assert
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(1)).save(accountCaptor.capture());

        Account savedAccount = accountCaptor.getValue();
        assertEquals(testUser, savedAccount.getUser());
        assertEquals(testSystem, savedAccount.getSystem());
        assertEquals("testuser@test_system", savedAccount.getAccountLogin());
        assertEquals(AccountStatus.ACTIVE, savedAccount.getStatus());
        assertEquals(ProvisioningStatus.SUCCESS, savedAccount.getProvisioningStatus());

        ArgumentCaptor<RoleAssignment> assignmentCaptor = ArgumentCaptor.forClass(RoleAssignment.class);
        verify(roleAssignmentRepository, times(1)).save(assignmentCaptor.capture());

        RoleAssignment savedAssignment = assignmentCaptor.getValue();
        assertEquals(testUser, savedAssignment.getUser());
        assertEquals(testRole, savedAssignment.getRole());
        assertEquals(AssignType.DIRECT, savedAssignment.getAssignmentType());
    }

    @Test
    void testCreateAccount_SystemNotExists_CreatesSystem() {
        // Arrange
        when(targetSystemRepository.findByName("NEW_SYSTEM")).thenReturn(Optional.empty());
        when(targetSystemRepository.save(any(TargetSystem.class))).thenReturn(testSystem);
        when(roleRepository.findByName("TEST_ROLE")).thenReturn(Optional.of(testRole));
        when(accountRepository.findByUserIdAndSystemId(1L, 1)).thenReturn(Optional.empty());

        // Act
        provisioningService.createAccount(testUser, "NEW_SYSTEM", "TEST_ROLE");

        // Assert
        verify(targetSystemRepository, times(1)).save(any(TargetSystem.class));
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testCreateAccount_RoleNotExists_CreatesRole() {
        // Arrange
        when(targetSystemRepository.findByName("TEST_SYSTEM")).thenReturn(Optional.of(testSystem));
        when(roleRepository.findByName("NEW_ROLE")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);
        when(accountRepository.findByUserIdAndSystemId(1L, 1)).thenReturn(Optional.empty());

        // Act
        provisioningService.createAccount(testUser, "TEST_SYSTEM", "NEW_ROLE");

        // Assert
        verify(roleRepository, times(1)).save(any(Role.class));
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testCreateAccount_AccountExists_NoOp() {
        // Arrange
        Account existingAccount = Account.builder()
                .id(1)
                .user(testUser)
                .system(testSystem)
                .accountLogin("testuser@TEST_SYSTEM")
                .status(AccountStatus.ACTIVE)
                .build();

        when(targetSystemRepository.findByName("TEST_SYSTEM")).thenReturn(Optional.of(testSystem));
        when(roleRepository.findByName("TEST_ROLE")).thenReturn(Optional.of(testRole));
        when(accountRepository.findByUserIdAndSystemId(1L, 1)).thenReturn(Optional.of(existingAccount));

        // Act
        provisioningService.createAccount(testUser, "TEST_SYSTEM", "TEST_ROLE");

        // Assert
        verify(accountRepository, never()).save(any(Account.class));
        verify(roleAssignmentRepository, never()).save(any(RoleAssignment.class));
    }

    @Test
    void testBlockAccount_SetsDisabledStatus() {
        // Arrange
        Account existingAccount = Account.builder()
                .id(1)
                .user(testUser)
                .system(testSystem)
                .accountLogin("testuser@TEST_SYSTEM")
                .status(AccountStatus.ACTIVE)
                .provisioningStatus(ProvisioningStatus.SUCCESS)
                .build();

        when(targetSystemRepository.findByName("TEST_SYSTEM")).thenReturn(Optional.of(testSystem));
        when(accountRepository.findByUserIdAndSystemId(1L, 1)).thenReturn(Optional.of(existingAccount));

        // Act
        provisioningService.blockAccount(testUser, "TEST_SYSTEM");

        // Assert
        assertEquals(AccountStatus.DISABLED, existingAccount.getStatus());
        verify(accountRepository, times(1)).save(existingAccount);
    }

    @Test
    void testBlockAccount_SystemNotFound_NoOp() {
        // Arrange
        when(targetSystemRepository.findByName("UNKNOWN_SYSTEM")).thenReturn(Optional.empty());

        // Act
        provisioningService.blockAccount(testUser, "UNKNOWN_SYSTEM");

        // Assert
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testBlockAccount_AccountNotFound_NoOp() {
        // Arrange
        when(targetSystemRepository.findByName("TEST_SYSTEM")).thenReturn(Optional.of(testSystem));
        when(accountRepository.findByUserIdAndSystemId(1L, 1)).thenReturn(Optional.empty());

        // Act
        provisioningService.blockAccount(testUser, "TEST_SYSTEM");

        // Assert
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testCompleteRequest_UpdatesStatusToCompleted() {
        // Arrange
        when(requestRepository.findById(1)).thenReturn(Optional.of(testRequest));

        // Act
        provisioningService.completeRequest(1);

        // Assert
        assertEquals(RequestStatus.COMPLETED, testRequest.getStatus());
        assertNotNull(testRequest.getResolvedAt());
        verify(requestRepository, times(1)).save(testRequest);
    }

    @Test
    void testCompleteRequest_RequestNotFound_ThrowsException() {
        // Arrange
        when(requestRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            provisioningService.completeRequest(999);
        });
    }

    @Test
    void testCompleteRequest_ResolvedAtIsSet() {
        // Arrange
        LocalDateTime before = LocalDateTime.now();
        when(requestRepository.findById(1)).thenReturn(Optional.of(testRequest));

        // Act
        provisioningService.completeRequest(1);

        // Assert
        assertTrue(testRequest.getResolvedAt().isAfter(before.minusSeconds(1)));
        assertTrue(testRequest.getResolvedAt().isBefore(before.plusSeconds(1)));
    }
}
