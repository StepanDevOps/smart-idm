# 📚 Документация по юнит-тестам Smart IDM

## Обзор

В проекте реализовано **5 юнит-кейсов** для тестирования сервисного слоя приложения. Все тесты используют **Mockito** для мокирования зависимостей и **JUnit 5** для выполнения тестов.

---

## 📋 Список тестов

| Тест | Сервис | Тестов | Назначение |
|------|--------|--------|------------|
| `AuditServiceTest` | AuditServiceImpl | 5 | Логирование событий безопасности |
| `DepartmentRoleServiceImplTest` | DepartmentRoleServiceImpl | 5 | INDIRECT роли через департаменты |
| `IdentityServiceImplTest` | IdentityServiceImpl | 5 | HR-события (Joiner/Mover/Leaver) |
| `RequestServiceImplTest` | RequestServiceImpl | 5 | Workflow согласования заявок |
| `ProvisioningServiceImplTest` | ProvisioningServiceImpl | 10 | Создание/блокировка аккаунтов |

**Итого:** 30 тестов

---

## 🔧 Запуск тестов

```bash
# Все тесты
mvn test

# Конкретный тест
mvn test -Dtest=ProvisioningServiceImplTest

# Отчёт о покрытии (требуется плагин jacoco-maven-plugin)
mvn test jacoco:report

# Открыть отчёт (Windows)
start target\site\jacoco\index.html
```

---

## 📖 Описание тестов

### 1. AuditServiceTest

**Сервис:** `AuditServiceImpl`  
**Назначение:** Тестирование логирования событий безопасности

| Тест | Описание |
|------|----------|
| `testLogAction_SavesSecurityLog` | Проверка сохранения события в лог |
| `testLogAction_NullUser_SystemEvent` | Логирование системного события без пользователя |
| `testLogAction_ExceptionHandling_DoesNotThrow` | Обработка исключений — не бросает ошибку |
| `testLogAction_EventTimeIsSet` | Проверка установки времени события |
| `testLogAction_MultipleEvents` | Логирование нескольких событий |

**Пример:**
```java
auditService.logAction(user, "LOGIN_SUCCESS", "Пользователь вошёл в систему");
```

---

### 2. DepartmentRoleServiceImplTest

**Сервис:** `DepartmentRoleServiceImpl`  
**Назначение:** Тестирование INDIRECT назначений ролей через департаменты

| Тест | Описание |
|------|----------|
| `testGetAllRolesForDepartmentWithChildren_IncludesParentAndChildRoles` | Получение ролей родителя и детей |
| `testGetAllRolesForDepartmentWithChildren_NoChildren_ReturnsOnlyParentRoles` | Только роли родителя |
| `testGetAllRolesForDepartmentWithChildren_NoRoles_ReturnsEmpty` | Нет ролей — пустой список |
| `testCreateAssignment_SavesDepartmentRole` | Создание связи департамент-роль |
| `testDeleteAssignment_ById` | Удаление связи по ID |

**Пример:**
```java
// Сотрудник получает роли департамента автоматически
List<Integer> roleIds = departmentRoleService.getAllRolesForDepartmentWithChildren(1);
// [100, 200] — роли родителя и дочернего отдела
```

---

### 3. IdentityServiceImplTest

**Сервис:** `IdentityServiceImpl`  
**Назначение:** Тестирование обработки HR-событий (Joiner/Mover/Leaver)

| Тест | Описание |
|------|----------|
| `testProcessJoiner_SetsActiveStatus` | Приём на работу — статус ACTIVE |
| `testProcessJoiner_AssignsIndirectRoles` | Назначение INDIRECT ролей при приёме |
| `testProcessLeaver_SetsTerminatedStatus` | Увольнение — статус TERMINATED |
| `testProcessLifecycleEvent_UnknownEvent_ReturnsFalse` | Неизвестное событие — false |
| `testProcessLifecycleEvent_NullUser_ReturnsFalse` | Null пользователь — false |

**Пример:**
```java
// Joiner — приём на работу
identityService.processJoiner(user, "Приём на работу");

// Leaver — увольнение
identityService.processLeaver(user, "Увольнение");
```

---

### 4. RequestServiceImplTest

**Сервис:** `RequestServiceImpl`  
**Назначение:** Тестирование workflow согласования заявок на доступ

| Тест | Описание |
|------|----------|
| `testCreateAccessRequest_CreatesRequestAndFirstStep` | Создание заявки и первого этапа |
| `testResolveRequestStep_LineManagerApproves_MovesToNext` | ЛР одобрил — переход к ИБ |
| `testResolveRequestStep_LineManagerRejects_RequestRejected` | ЛР отклонил — заявка отклонена |
| `testResolveRequestStep_SecurityOfficerApproves_CompletesRequest` | ИБ одобрил — provisioning |
| `testResolveRequestStep_NoActiveStep_ReturnsFalse` | Нет активного этапа — false |

**Пример:**
```java
// Создание заявки
Request request = requestService.createAccessRequest(
    requestor, requestedFor, "Role", "System", "Обоснование"
);

// Согласование
requestService.resolveRequestStep(requestId, approverId, true); // true = одобрить
```

**Workflow:**
```
CREATED → LINE_MANAGER → SECURITY_OFFICER → COMPLETED
                              ↓
                        (provisioning)
```

---

### 5. ProvisioningServiceImplTest

**Сервис:** `ProvisioningServiceImpl`  
**Назначение:** Тестирование создания и блокировки учётных записей

| Тест | Описание |
|------|----------|
| `testCreateAccount_CreatesAccountAndRole` | Создание аккаунта и роли |
| `testCreateAccount_SystemNotExists_CreatesSystem` | Создание системы при отсутствии |
| `testCreateAccount_RoleNotExists_CreatesRole` | Создание роли при отсутствии |
| `testCreateAccount_AccountExists_NoOp` | Аккаунт существует — пропуск |
| `testBlockAccount_SetsDisabledStatus` | Блокировка аккаунта |
| `testBlockAccount_SystemNotFound_NoOp` | Система не найдена — пропуск |
| `testBlockAccount_AccountNotFound_NoOp` | Аккаунт не найден — пропуск |
| `testCompleteRequest_UpdatesStatusToCompleted` | Завершение заявки |
| `testCompleteRequest_RequestNotFound_ThrowsException` | Заявка не найдена — исключение |
| `testCompleteRequest_ResolvedAtIsSet` | Установка времени завершения |

**Пример:**
```java
// Создание учётной записи
provisioningService.createAccount(user, "AD", "UserRole");

// Блокировка
provisioningService.blockAccount(user, "AD");

// Завершение заявки
provisioningService.completeRequest(requestId);
```

---

## 🏗 Архитектура тестов

### Структура теста (AAA Pattern)

```java
@Test
void testExample() {
    // Arrange — подготовка данных
    User user = new User();
    when(userRepository.save(any())).thenReturn(user);
    
    // Act — вызов метода
    identityService.processJoiner(user, "Приём");
    
    // Assert — проверка результата
    assertEquals(UserStatus.ACTIVE, user.getStatus());
    verify(userRepository, times(1)).save(user);
}
```

### Используемые аннотации

| Аннотация | Назначение |
|-----------|------------|
| `@ExtendWith(MockitoExtension.class)` | Подключение Mockito |
| `@Mock` | Создание мока |
| `@InjectMocks` | Внедрение моков в тестируемый сервис |
| `@BeforeEach` | Инициализация перед каждым тестом |
| `@Test` | Объявление теста |

### Mockito методы

| Метод | Назначение |
|-------|------------|
| `when(...).thenReturn(...)` | Stubbing — возврат значения |
| `verify(..., times(n)).method()` | Проверка вызова метода |
| `ArgumentCaptor.forClass(...)` | Перехват аргументов |
| `doNothing().when(...)` | Stubbing для void методов |
| `eq(...)` | Точное совпадение аргумента |
| `any(...)` | Любой аргумент |

---

## 📊 Покрытие кода

Для генерации отчёта о покрытии:

```bash
mvn test jacoco:report
```

Отчёт будет доступен в: `target/site/jacoco/index.html`

---

## 🎯 Best Practices

### ✅ Делать

1. **Именовать тесты понятно:** `testCreateAccount_CreatesAccountAndRole`
2. **Следовать AAA паттерну:** Arrange → Act → Assert
3. **Мокировать внешние зависимости:** Репозитории, внешние сервисы
4. **Тестировать граничные случаи:** Null, пустые списки, исключения
5. **Использовать понятные сообщения:** `assertEquals(expected, actual, "Сообщение")`

### ❌ Не делать

1. **Не тестировать несколько сценариев в одном тесте**
2. **Не использовать реальные БД в юнит-тестах**
3. **Не игнорировать исключения**
4. **Не писать тесты без Assert'ов**
