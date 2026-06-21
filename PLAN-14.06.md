

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
- [x] `GET /role-assignments` — список всех назначений
- [x] `GET /role-assignments/{id}` — детальный просмотр
- [x] `POST /role-assignments` — создать назначение
- [x] `PUT /role-assignments/{id}` — обновить назначение
- [x] `DELETE /role-assignments/{id}` — отозвать назначение

**Файлы:**
- `src/main/java/ru/mtkp/idm/controller/RoleAssignmentController.java`
- `src/main/resources/templates/role-assignments.html`
- `src/main/resources/templates/role-assignment-form.html`
- `src/main/resources/templates/role-assignment-detail.html`

**Статус:** ✅ Выполнено

---

### 2.2. Сервис валидации
**Задачи:**
- [x] Создать `Validator` для `RoleAssignment`
- [x] Проверка: `effectiveTo >= effectiveFrom`
- [x] Проверка: пользователь не уволен (`status != TERMINATED`)

**Файлы:**
- `src/main/java/ru/mtkp/idm/validator/RoleAssignmentValidator.java`

**Статус:** ✅ Выполлено

---

### 2.3. Улучшение AccountController
**Задачи:**
- [x] Добавить метод `assignRoleToAccount(accountId, roleId)`
- [x] Добавить метод `revokeRoleFromAccount(accountId, roleId)`
- [x] Добавить валидацию: аккаунт существует, роль существует

**Файлы:**
- `src/main/java/ru/mtkp/idm/controller/AccountController.java`

**Статус:** ✅ Выполлено

---

### 2.4. Структура организации и фильтрация ролей
**Задачи:**
- [x] Проверить сущность `Department` (само-ссылка для дерева)
- [x] Реализовать выбор системы перед выбором роли (UI)
- [x] Показывать только глобальные роли (без системы) или роли конкретной системы
- [x] Добавить AJAX-эндпоинт `/role-assignments/roles/{systemId}` для фильтрации
- [ ] Реализовать выбор департамента перед выбором роли (INDIRECT)
- [ ] Показывать роли, доступные для департамента (через future `DepartmentRole` mapping)
- [ ] Реализовать авто-назначение ролей при HR-событиях на основе орг-структуры

**Файлы:**
- `src/main/java/ru/mtkp/idm/controller/RoleAssignmentController.java`
- `src/main/resources/templates/role-assignment-form.html`
- `src/main/java/ru/mtkp/idm/repository/RoleRepository.java`
- `src/main/java/ru/mtkp/idm/model/Department.java` *(future)*
- `src/main/java/ru/mtkp/idm/repository/DepartmentRepository.java` *(future)*

**Статус:** 🟡 Частично выполнено (UI для DIRECT — ✅, INDIRECT — ⬜)

---

## 🏗 Приоритет 3: Иерархия департаментов (INDIRECT assignments)

> **Цель:** Поддержка автоматического (INDIRECT) назначения ролей на основе организационной структуры.

### 3.1. Дерево департаментов
**Задачи:**
- [x] Проверить само-ссылку в `Department` (`parentUnit` → `Department`)
- [x] Добавить репозиторный метод для получения всех подчинённых департаментов (рекурсивно)
- [x] Добавить методы для проверки, является ли департамент родителем/потомком другого
- [x] Добавить валидацию: нельзя назначить департамент родителем самого себя

**Файлы:**
- `src/main/java/ru/mtkp/idm/model/Department.java`
- `src/main/java/ru/mtkp/idm/repository/DepartmentRepository.java`

**Ожидаемый JPQL для рекурсии:**
```java
// Поиск всех потомков (уровень-зависимый, N+1)
@Query("SELECT d FROM Department d WHERE d.parent.id = :parentId")
List<Department> findChildren(Integer parentId);

// Полный путь (path) через tree traversal — требует PostgreSQL pg_pathman или рекурсивного CTE
@Query(value = "WITH RECURSIVE dept_tree AS (" +
    "  SELECT id, parent_id, 1 AS level, ARRAY[id] AS path FROM department WHERE parent_id IS NULL" +
    "  UNION ALL" +
    "  SELECT d.id, d.parent_id, dt.level + 1, dt.path || d.id " +
    "  FROM department d JOIN dept_tree dt ON d.parent_id = dt.id" +
") SELECT * FROM dept_tree ORDER BY level", nativeQuery = true)
List<Map<String, Object>> findAllWithPath();
```

**Статус:** ✅ Выполнено

---

### 3.2. Mapping департамент → роли
**Задачи:**
- [x] Создать сущность `DepartmentRole` (многие-ко-многим: `Department` ↔ `Role`)
- [x] Добавить в `RoleAssignment` тип назначения: `DIRECT` (через User) или `INDIRECT` (через Department)
- [x] Обновить `RoleAssignmentValidator` для проверки INDIRECT назначений
- [x] Добавить метод `getRolesByDepartment(departmentId)` в `DepartmentRoleService`

**Файлы:**
- `src/main/java/ru/mtkp/idm/model/DepartmentRole.java`
- `src/main/java/ru/mtkp/idm/model/RoleAssignment.java`
- `src/main/java/ru/mtkp/idm/repository/DepartmentRoleRepository.java`
- `src/main/java/ru/mtkp/idm/service/DepartmentRoleService.java`
- `src/main/java/ru/mtkp/idm/validator/RoleAssignmentValidator.java`

**Статус:** ✅ Выполнено

---

### 3.2. Mapping департамент → роли
**Задачи:**
- [ ] Создать сущность `DepartmentRole` (многие-ко-многим: `Department` ↔ `Role`)
- [ ] Добавить в `RoleAssignment` тип назначения: `DIRECT` (через User) или `INDIRECT` (через Department)
- [ ] Обновить `RoleAssignmentValidator` для проверки INDIRECT назначений
- [ ] Добавить метод `getRolesByDepartment(departmentId)` в `RoleRepository`

**Файлы:**
- `src/main/java/ru/mtkp/idm/model/DepartmentRole.java` *(new)*
- `src/main/java/ru/mtkp/idm/model/RoleAssignment.java` *(update)*
- `src/main/java/ru/mtkp/idm/repository/DepartmentRoleRepository.java` *(new)*

**Статус:** ⬜ Не начато

---

### 3.3. Интеграция с HR-событиями
**Задачи:**
- [x] При создании/изменении пользователя проверять его департамент
- [x] Если департамент имеет mapped роли — создать `RoleAssignment` с типом `INDIRECT`
- [x] Добавить `WorkflowEngineService` триггер на HR-события (`USER_CREATED`, `DEPARTMENT_CHANGED`)
- [x] Логировать автоматические назначения в `SecurityLog`

**Файлы:**
- `src/main/java/ru/mtkp/idm/service/WorkflowEngineService.java`
- `src/main/java/ru/mtkp/idm/service/IdentityService.java`
- `src/main/java/ru/mtkp/idm/service/impl/IdentityServiceImpl.java`
- `src/main/java/ru/mtkp/idm/model/SecurityLog.java`

**Статус:** ✅ Выполнено

---

### 3.4. UI для управления INDIRECT назначениями
**Задачи:**
- [x] Форма: выбор департамента → отображение mapped ролей → кнопка «Создать связь»
- [x] Таблица: список всех INDIRECT назначений с фильтрацией по департаменту
- [x] Обновлена форма назначения ролей с выбором типа DIRECT/INDIRECT

**Файлы:**
- `src/main/java/ru/mtkp/idm/controller/DepartmentRoleController.java`
- `src/main/resources/templates/department-roles.html`
- `src/main/resources/templates/department-role-form.html`
- `src/main/resources/templates/role-assignment-form.html`

**Статус:** ✅ Выполнено

---

## 🟢 Приоритет 4: Дополнительные возможности

### 4.1. Аудит и логирование
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

### 4.2. Массовые операции
**Задачи:**
- [ ] Массовое назначение ролей (несколько ролей сразу)
- [ ] Массовый отзыв ролей

**Статус:** ⬜ Не выполнено

---

### 4.3. API для внешних систем
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
