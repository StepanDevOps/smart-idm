package ru.mtkp.idm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mtkp.idm.model.*;
import ru.mtkp.idm.repository.ApprovalStepRepository;
import ru.mtkp.idm.repository.RequestRepository;
import ru.mtkp.idm.repository.RoleRepository;
import ru.mtkp.idm.repository.UserRepository;
import ru.mtkp.idm.service.impl.ProvisioningServiceImpl;
import ru.mtkp.idm.service.impl.RequestServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

/**
 * Unit-тесты для RequestServiceImpl.
 * Проверяют workflow согласования заявок.
 */
@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ApprovalStepRepository approvalStepRepository;

    @Mock
    private ProvisioningServiceImpl provisioningService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private RequestServiceImpl requestService;

    private User requestor;
    private User requestedFor;
    private User lineManager;
    private User securityOfficer;
    private Role testRole;
    private Request testRequest;

    @BeforeEach
    void setUp() {
        requestor = User.builder()
                .id(1L)
                .login("requestor")
                .firstName("Иван")
                .lastName("Иванов")
                .emailWork("ivanov@test.ru")
                .hireDate(LocalDate.now())
                .status(UserStatus.ACTIVE)
                .build();

        lineManager = User.builder()
                .id(3L)
                .login("manager")
                .firstName("Сергей")
                .lastName("Сергеев")
                .emailWork("sergeev@test.ru")
                .hireDate(LocalDate.now())
                .status(UserStatus.ACTIVE)
                .build();

        requestedFor = User.builder()
                .id(2L)
                .login("requested")
                .firstName("Петр")
                .lastName("Петров")
                .emailWork("petrov@test.ru")
                .hireDate(LocalDate.now())
                .status(UserStatus.ACTIVE)
                .manager(lineManager) // ЛР
                .build();

        securityOfficer = User.builder()
                .id(4L)
                .login("security")
                .firstName("Админ")
                .lastName("ИБ")
                .emailWork("security@test.ru")
                .hireDate(LocalDate.now())
                .status(UserStatus.ACTIVE)
                .build();

        testRole = Role.builder()
                .id(100)
                .name("Тестовая роль")
                .description("Описание")
                .build();

        testRequest = Request.builder()
                .id(1)
                .requestor(requestor)
                .requestedFor(requestedFor)
                .role(testRole)
                .status(RequestStatus.CREATED)
                .justification("Тест")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateAccessRequest_CreatesRequestAndFirstStep() {
        // Arrange
        when(roleRepository.findByName("Тестовая роль")).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);
        when(requestRepository.save(any(Request.class))).thenReturn(testRequest);
        when(approvalStepRepository.save(any(ApprovalStep.class))).thenReturn(new ApprovalStep());

        // Act
        Request result = requestService.createAccessRequest(
                requestor, requestedFor, "Тестовая роль", "TEST_SYSTEM", "Обоснование");

        // Assert
        assertNotNull(result);
        assertEquals(RequestStatus.CREATED, result.getStatus());
        verify(requestRepository, times(1)).save(any(Request.class));
        verify(approvalStepRepository, times(1)).save(argThat(step -> 
                step.getStepName() == StepName.LINE_MANAGER));
        verify(auditService, times(1)).logAction(eq(requestor), eq("ACCESS_REQUEST_CREATED"), anyString());
    }

    @Test
    void testResolveRequestStep_LineManagerApproves_MovesToSecurityOfficer() {
        // Arrange
        ApprovalStep lineManagerStep = ApprovalStep.builder()
                .id(1)
                .request(testRequest)
                .stepName(StepName.LINE_MANAGER)
                .decision(AppDecision.PENDING)
                .build();

        when(requestRepository.findById(1)).thenReturn(Optional.of(testRequest));
        when(userRepository.findById(3L)).thenReturn(Optional.of(lineManager));
        when(approvalStepRepository.findByRequestId(1)).thenReturn(List.of(lineManagerStep));
        when(approvalStepRepository.save(any(ApprovalStep.class))).thenReturn(lineManagerStep);

        // Act
        boolean result = requestService.resolveRequestStep(1, 3L, true);

        // Assert
        assertTrue(result);
        verify(auditService, times(1)).logAction(eq(lineManager), eq("REQUEST_COMPLETED"), anyString());
    }

    @Test
    void testResolveRequestStep_LineManagerRejects_RequestRejected() {
        // Arrange
        ApprovalStep lineManagerStep = ApprovalStep.builder()
                .id(1)
                .request(testRequest)
                .stepName(StepName.LINE_MANAGER)
                .decision(AppDecision.PENDING)
                .build();

        when(requestRepository.findById(1)).thenReturn(Optional.of(testRequest));
        when(userRepository.findById(3L)).thenReturn(Optional.of(lineManager));
        when(approvalStepRepository.findByRequestId(1)).thenReturn(List.of(lineManagerStep));
        when(approvalStepRepository.save(any(ApprovalStep.class))).thenReturn(lineManagerStep);

        // Act
        boolean result = requestService.resolveRequestStep(1, 3L, false);

        // Assert
        assertTrue(result);
        assertEquals(RequestStatus.REJECTED, testRequest.getStatus());
        verify(auditService, times(1)).logAction(eq(lineManager), eq("REQUEST_REJECTED"), anyString());
    }

    @Test
    void testResolveRequestStep_SecurityOfficerApproves_CompletesRequest() {
        // Arrange
        ApprovalStep lineManagerStep = ApprovalStep.builder()
                .id(1)
                .request(testRequest)
                .stepName(StepName.LINE_MANAGER)
                .decision(AppDecision.APPROVED)
                .build();

        ApprovalStep securityOfficerStep = ApprovalStep.builder()
                .id(2)
                .request(testRequest)
                .stepName(StepName.SECURITY_OFFICER)
                .decision(AppDecision.PENDING)
                .build();

        when(requestRepository.findById(1)).thenReturn(Optional.of(testRequest));
        when(userRepository.findById(4L)).thenReturn(Optional.of(securityOfficer));
        when(approvalStepRepository.findByRequestId(1)).thenReturn(List.of(lineManagerStep, securityOfficerStep));
        when(approvalStepRepository.save(any(ApprovalStep.class))).thenReturn(securityOfficerStep);
        doNothing().when(provisioningService).createAccount(any(User.class), anyString(), anyString());
        doNothing().when(provisioningService).completeRequest(anyInt());

        // Act
        boolean result = requestService.resolveRequestStep(1, 4L, true);

        // Assert
        assertTrue(result);
        assertEquals(RequestStatus.COMPLETED, testRequest.getStatus());
        verify(auditService, times(1)).logAction(eq(securityOfficer), eq("REQUEST_COMPLETED"), anyString());
        verify(provisioningService, times(1)).createAccount(eq(requestedFor), anyString(), anyString());
        verify(provisioningService, times(1)).completeRequest(eq(1));
    }

    @Test
    void testResolveRequestStep_NoActiveStep_ReturnsFalse() {
        // Arrange
        Request request = Request.builder()
                .id(2)
                .requestor(requestor)
                .requestedFor(requestedFor)
                .role(testRole)
                .status(RequestStatus.CREATED)
                .build();

        when(requestRepository.findById(2)).thenReturn(Optional.of(request));
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(approvalStepRepository.findByRequestId(2)).thenReturn(List.of());

        // Act
        boolean result = requestService.resolveRequestStep(2, 1L, true);

        // Assert
        assertFalse(result);
    }
}
