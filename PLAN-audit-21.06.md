## 📊 Анализ текущего состояния аудита

### ✅ Что уже есть:
| Компонент                | Статус      | Описание                                                                           |
|--------------------------|-------------|------------------------------------------------------------------------------------|
| `SecurityLog` (сущность) | ✅ Есть      | Таблица `security_logs` с полями: id, event_time, user_id, event_type, description |
| `SecurityLogRepository`  | ✅ Есть      | Базовые методы + фильтрация по userId, eventTime, eventType                        |
| Логирование заявок       | ✅ Есть      | `RequestServiceImpl.writeAuditLog()` — CREATE, APPROVE, REJECT, COMPLETE           |
| Логирование HR-событий   | ✅ Есть      | `IdentityServiceImpl.writeAuditLog()` — JOINER, MOOVER, LEAVER                     |
| Страница `/audit`        | ⚠️ Частично | Выводит заявки, а не SecurityLog                                                   |
| Меню "Журнал аудита"     | ✅ Есть      | В сайдбаре                                                                         |

### ❌ Чего нет:
1. **UI для SecurityLog** — страница показывает заявки, а не логи
2. **Логирование действий администратора** — создание/удаление департаментов, назначение ролей
3. **Логирование RoleAssignment** — создание/отзыв назначений
4. **Фильтрация и пагинация** — нет UI для фильтрации по дате, типу события, пользователю
5. **Дублирование логики** — `writeAuditLog()` скопирован в 2 сервисах

---

## 📋 План реализации аудита (Приоритет 4.1)

### Этап 1: Выделение AuditService (минимальные изменения)
**Цель:** Убрать дублирование `writeAuditLog()`, создать единый сервис

| Задача                                                               | Файлы                                                             | Сложность |
|----------------------------------------------------------------------|-------------------------------------------------------------------|-----------|
| Создать `AuditService` интерфейс                                     | `src/main/java/ru/mtkp/idm/service/AuditService.java`             | ⭐ Легко   |
| Реализовать `AuditServiceImpl` с `@Service` + `@Transactional`       | `src/main/java/ru/mtkp/idm/service/impl/AuditServiceImpl.java`    | ⭐ Легко   |
| Метод: `logAction(User user, String eventType, String description)`  | —                                                                 | ⭐ Легко   |
| Заменить `writeAuditLog()` в `RequestServiceImpl` на `AuditService`  | `src/main/java/ru/mtkp/idm/service/impl/RequestServiceImpl.java`  | ⭐⭐ Средне |
| Заменить `writeAuditLog()` в `IdentityServiceImpl` на `AuditService` | `src/main/java/ru/mtkp/idm/service/impl/IdentityServiceImpl.java` | ⭐⭐ Средне |

**Время:** ~15 минут  
**Риск:** Низкий (замена private-метода на injection)

---

### Этап 2: Добавление логирования ключевых действий
**Цель:** Логировать все критические операции IDM

#### 2.1. Логирование управления департаментами
| Действие | eventType            | Описание                     | Файл                        |
|----------|----------------------|------------------------------|-----------------------------|
| Создание | `DEPARTMENT_CREATED` | "Создан департамент: {name}" | `DepartmentController.java` |
| Удаление | `DEPARTMENT_DELETED` | "Удалён департамент: {name}" | `DepartmentController.java` |

#### 2.2. Логирование назначения/отзыва ролей
| Действие          | eventType                     | Описание                                               | Файл                            |
|-------------------|-------------------------------|--------------------------------------------------------|---------------------------------|
| Создание DIRECT   | `ROLE_ASSIGNMENT_CREATED`     | "Назначена роль {roleName} пользователю {userLogin}"   | `RoleAssignmentController.java` |
| Отзыв роли        | `ROLE_ASSIGNMENT_REVOKED`     | "Отозвана роль {roleName} у {userLogin}"               | `RoleAssignmentController.java` |
| Создание INDIRECT | `INDIRECT_ASSIGNMENT_CREATED` | "INDIRECT роль {roleName} для департамента {deptName}" | `DepartmentRoleController.java` |

#### 2.3. Логирование управления ролями
| Действие      | eventType      | Описание                   | Файл                  |
|---------------|----------------|----------------------------|-----------------------|
| Создание роли | `ROLE_CREATED` | "Создана роль: {roleName}" | `RoleController.java` |
| Удаление роли | `ROLE_DELETED` | "Удалена роль: {roleName}" | `RoleController.java` |

**Время:** ~30 минут  
**Риск:** Низкий (добавление вызовов в существующие методы)

---

### Этап 3: Обновление UI аудита
**Цель:** Показать реальные SecurityLog вместо заявок

| Задача                                                                          | Файлы                                 | Сложность   |
|---------------------------------------------------------------------------------|---------------------------------------|-------------|
| Обновить `AdminController.audit()` для загрузки SecurityLog                     | `AdminController.java`                | ⭐⭐ Средне   |
| Добавить `SecurityLogRepository.findAll(Pageable)` для пагинации                | `SecurityLogRepository.java`          | ⭐ Легко     |
| Обновить `audit.html` — таблица с колонками: время, пользователь, тип, описание | `audit.html`                          | ⭐⭐ Средне   |
| Добавить фильтры: по дате, типу события, пользователю                           | `audit.html` + `AdminController.java` | ⭐⭐⭐ Сложнее |
| Добавить пагинацию (Spring Data Page)                                           | `AdminController.java` + `audit.html` | ⭐⭐ Средне   |

**Пример таблицы:**
```html
<table>
  <thead>
    <tr>
      <th>Время</th>
      <th>Пользователь</th>
      <th>Тип события</th>
      <th>Описание</th>
    </tr>
  </thead>
  <tbody>
    <tr th:each="log : ${logs}">
      <td th:text="${#temporals.format(log.eventTime, 'yyyy-MM-dd HH:mm')}">-</td>
      <td th:text="${log.user != null ? log.user.login : 'system'}">-</td>
      <td><span class="badge" th:text="${log.eventType}">-</span></td>
      <td th:text="${log.description}">-</td>
    </tr>
  </tbody>
</table>
```

**Время:** ~45 минут  
**Риск:** Средний (изменение UI, нужно тестировать)

---

### Этап 4: Цветовая кодировка типов событий
**Цель:** Визуально различать типы событий

| eventType            | Цвет badge | Иконка |
|----------------------|------------|--------|
| `HR_EVENT_*`         | Зеленый    | 👤     |
| `ACCESS_REQUEST_*`   | Синий      | 📋     |
| `ROLE_*`             | Оранжевый  | 🔑     |
| `DEPARTMENT_*`       | Фиолетовый | 🏢     |
| `*ERROR` / `*DENIED` | Красный    | ❌      |

**Время:** ~15 минут  
**Риск:** Низкий (только CSS/Thymeleaf)

---

## 📊 Итоговая оценка

| Этап                      | Время         | Сложность  |
|---------------------------|---------------|------------|
| 1. Выделение AuditService | 15 мин        | ⭐ Легко    |
| 2. Логирование действий   | 30 мин        | ⭐⭐ Средне  |
| 3. Обновление UI аудита   | 45 мин        | ⭐⭐⭐ Средне |
| 4. Цветовая кодировка     | 15 мин        | ⭐ Легко    |
| **Итого**                 | **~1.5 часа** |            |

---

## ⚠️ Ключевые моменты

1. **SecurityLog уже есть** — не нужно создавать новую сущность
2. **Минимальные изменения в модели** — только добавление eventType в существующую таблицу
3. **Нет миграций БД** — поле `event_type` уже есть (length=100)
4. **Безопасность** — `@PreAuthorize("hasRole('ADMIN')")` на `/audit` (уже есть через SecurityConfig)

---

## 🚀 Рекомендуемый порядок реализации

1. **Этап 1** — рефакторинг (убрать дублирование)
2. **Этап 2** — добавить логирование (бэкенд)
3. **Этап 3** — обновить UI (фронтенд)
4. **Этап 4** — polish (цветовая кодировка)