

# План развития бэкенда Smart IDM

**Дата начала:** 14.06.2026  
**Статус:** В разработке

---

## 🔴 Приоритет 1: Критические доработки бэкенда

### 1.1. RoleAssignmentService
**Задачи:**
- [x] Создать `RoleAssignmentService` интерфейс
- [x] Создать `RoleAssignmentServiceImpl` реализацию
- [x] Методы:
  - [x] `assignRoleToUser(userId, roleId, reason, effectiveFrom, effectiveTo)`
  - [x] `revokeRoleAssignment(assignmentId)`
  - [x] `getActiveAssignments(userId)`
  - [x] `getExpiredAssignments()`
- [x] Добавить валидацию дат
- [x] Добавить аудит назначений

**Файлы:**
- `src/main/java/ru/mtkp/idm/service/RoleAssignmentService.java`
- `src/main/java/ru/mtkp/idm/service/impl/RoleAssignmentServiceImpl.java`

**Статус:** ✅ Выполнено

---

### 1.2. Репозиторий: методы для RoleAssignment
**Задачи:**
- [x] Добавить `findAllWithUserAndRole()`
- [x] Добавить `findByEffectiveToIsNullAndRole_Id(roleId)`
- [x] Добавить `findExpiredAssignments()` (где `effectiveTo < today`)

**Файлы:**
- `src/main/java/ru/mtkp/idm/repository/RoleAssignmentRepository.java`

**Статус:** ✅ Выполнено

---

### 1.3. DTO для назначения ролей
**Задачи:**
- [x] Создать `RoleAssignmentDTO`
- [x] Создать `RoleAssignmentRequest` (для форм)

**Файлы:**
- `src/main/java/ru/mtkp/idm/dto/RoleAssignmentDTO.java`
- `src/main/java/ru/mtkp/idm/dto/RoleAssignmentRequest.java`

**Статус:** ✅ Выполнено

---

### 1.4. Глобальный список учётных записей
**Задачи:**
- [x] Добавить в `AccountRepository` методы для фильтрации
- [x] Добавить в `AccountController` метод для общего списка:
  - `GET /accounts/all` — список всех аккаунтов
  - Параметры фильтрации: `userId`, `systemId`, `status`, `search`
- [x] Создать шаблон `accounts-all.html`
- [x] Добавить пункт меню "Учётные записи" в навигацию

**Файлы:**
- `src/main/java/ru/mtkp/idm/controller/AccountController.java`
- `src/main/resources/templates/accounts-all.html`
- `src/main/java/ru/mtkp/idm/repository/AccountRepository.java`
- `src/main/resources/templates/components/layout.html`

**Статус:** ✅ Выполнено

---

## 🟡 Приоритет 2: Расширение функционала

### 2.1. RoleAssignmentController
**Задачи:**
- [ ] `GET /role-assignments` — список всех назначений
- [ ] `GET /role-assignments/{id}` — детальный просмотр
- [ ] `POST /role-assignments` — создать назначение
- [ ] `PUT /role-assignments/{id}` — обновить назначение
- [ ] `DELETE /role-assignments/{id}` — отозвать назначение

**Файлы:**
- `src/main/java/ru/mtkp/idm/controller/RoleAssignmentController.java`

**Статус:** ⬜ Не выполнено

---

### 2.2. Сервис валидации
**Задачи:**
- [ ] Создать `Validator` для `RoleAssignment`
- [ ] Проверка: `effectiveTo >= effectiveFrom`
- [ ] Проверка: пользователь не уволен (`status != TERMINATED`)

**Файлы:**
- `src/main/java/ru/mtkp/idm/validator/RoleAssignmentValidator.java`

**Статус:** ⬜ Не выполнено

---

### 2.3. Улучшение AccountController
**Задачи:**
- [ ] Добавить метод `assignRoleToAccount(accountId, roleId)`
- [ ] Добавить метод `revokeRoleFromAccount(accountId, roleId)`
- [ ] Добавить валидацию: аккаунт существует, роль существует

**Статус:** ⬜ Не выполнено

---

## 🟢 Приоритет 3: Дополнительные возможности

### 3.1. Аудит и логирование
**Задачи:**
- [ ] Создать сущность `AuditLog`
- [ ] Добавить логирование всех изменений в `RoleAssignment`
- [ ] Добавить `SecurityLog` для действий администратора

**Файлы:**
- `src/main/java/ru/mtkp/idm/model/AuditLog.java`
- `src/main/java/ru/mtkp/idm/repository/AuditLogRepository.java`
- `src/main/java/ru/mtkp/idm/service/AuditService.java`

**Статус:** ⬜ Не выполнено

---

### 3.2. Массовые операции
**Задачи:**
- [ ] Массовое назначение ролей (несколько ролей сразу)
- [ ] Массовый отзыв ролей

**Статус:** ⬜ Не выполнено

---

### 3.3. API для внешних систем
**Задачи:**
- [ ] Создать REST контроллер для интеграции
- [ ] `POST /api/assignments` — создать назначение извне
- [ ] `GET /api/assignments/{userId}` — получить назначения

**Файлы:**
- `src/main/java/ru/mtkp/idm/controller/api/AssignmentApiController.java`

**Статус:** ⬜ Не выполнено

---

## 📝 Примечания

- Все изменения должны быть протестированы
- После каждого этапа запускать `mvn test`
- Следовать стилю кодирования проекта
- Использовать Lombok аннотации
- Следовать 3-слойной архитектуре (Controller → Service → Repository)
