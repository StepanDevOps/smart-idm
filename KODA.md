# KODA.md — Контекст проекта Smart IDM

## 📋 Обзор проекта

**Smart IDM** — учебный MVP системы Identity Management (IDM) для автоматизации управления пользователями и их учётными записями в целевых системах.

### Назначение

Проект предназначен для демонстрации базового IDM-workflow в рамках производственной практики по модулю ПМ.02 «Осуществление интеграции программных модулей».

### Бизнес-процессы

- **Joiner** — приём нового сотрудника и первичная выдача доступов
- **Mover** — изменение подразделения, должности или набора доступов
- **Leaver** — блокировка учётных записей и отзыв доступов при увольнении
- **Access Request** — заявка на выдачу или изменение доступа к целевой системе

---

## 🛠 Технологический стек

### Основные технологии

| Технология | Версия | Назначение |
|------------|--------|------------|
| Java | 21 | Язык разработки |
| Spring Boot | 4.0.6 | Фреймворк приложения |
| Spring Data JPA | — | Работа с БД |
| Spring Security | — | Аутентификация и авторизация |
| Thymeleaf | — | Шаблоны UI |
| PostgreSQL | — | Основная СУБД |
| H2 | — | Тестовая БД |
| Lombok | — | Генерация кода |
| Maven | — | Сборка проекта |
| dotenv-java | 3.0.0 | Работа с переменными окружения |

### Архитектура

**3-слойная архитектура:**
```
Controller → Service → Repository
```

**Пакеты проекта:**
```
ru.mtkp.idm
├── bootstrap/       — Инициализация данных
├── config/          — Конфигурация (Security, Web)
├── controller/      — Контроллеры (UI)
├── model/           — Доменные сущности
├── repository/      — Репозитории (JPA)
└── service/         — Бизнес-логика
```

---

## 📁 Структура проекта

### Ключевые директории

```
.
├── src/main/java/ru/mtkp/idm/
│   ├── bootstrap/           # Инициализация тестовых данных
│   ├── config/              # Spring Security, Web конфигурация
│   ├── controller/          # Контроллеры для UI
│   ├── model/               # Доменные сущности (JPA)
│   ├── repository/          # Spring Data JPA репозитории
│   ├── service/             # Бизнес-логика
│   └── SmartIdmApplication.java
├── src/main/resources/
│   ├── templates/           # Thymeleaf шаблоны
│   ├── static/              # Статические ресурсы
│   ├── application.yaml     # Конфигурация приложения
│   └── messages*.properties # Локализация
├── src/test/                # Тесты
└── pom.xml                  # Maven конфигурация
```

### Основные доменные сущности

| Сущность | Описание |
|----------|----------|
| `User` | Доменный пользователь (сотрудник) |
| `Account` | Учётная запись в целевой системе |
| `TargetSystem` | Целевая система (AD, 1C, SAP и т.д.) |
| `Role` | Роль/права доступа |
| `RoleAssignment` | Назначение роли пользователю |
| `Request` | Заявка на доступ |
| `ApprovalStep` | Шаг согласования заявки |
| `IdmUser` | Локальный пользователь IDM (для входа в систему) |
| `Department` | Подразделение организации |

---

## 🚀 Сборка и запуск

### Предварительные требования

- Java 21+
- PostgreSQL 14+ (или H2 для тестов)
- Maven 3.8+

### Команды

```bash
# Сборка проекта
mvn clean compile

# Запуск тестов
mvn test

# Сборка JAR
mvn package -DskipTests

# Запуск приложения
mvn spring-boot:run

# Или из скомпилированного JAR
java -jar target/idm-0.0.1-SNAPSHOT.jar
```

### Конфигурация

Приложение использует переменные окружения (файл `.env`):

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/smart_idm
SPRING_DATASOURCE_USERNAME=smart_idm
SPRING_DATASOURCE_PASSWORD=smart_idm
```

**По умолчанию в `application.yaml`:**
- Порт: `8080`
- БД: PostgreSQL на `localhost:5432/smart_idm`
- DDL: `update` (автоматическое обновление схемы)

### Доступ к приложению

- **URL:** `http://localhost:8080`
- **Логин:** `admin`
- **Пароль:** `admin`

---

## 🧪 Тестирование

```bash
# Запуск всех тестов
mvn test

# Тесты с покрытием
mvn test jacoco:report
```

**Структура тестов:**
- `SmartIdmApplicationTests.java` — интеграционные тесты

---

## 📝 Правила разработки

### Стиль кодирования

1. **Именование:**
   - Классы: `PascalCase` (например, `UserController`)
   - Методы: `camelCase` (например, `listUsers()`)
   - Переменные: `camelCase` (например, `userId`)
   - Константы: `UPPER_SNAKE_CASE`

2. **Аннотации Lombok:**
   - Используйте `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
   - Для сущностей JPA обязательно `@NoArgsConstructor` и `@AllArgsConstructor`

3. **Spring аннотации:**
   - Контроллеры: `@Controller`, `@RestController`
   - Сервисы: `@Service`
   - Репозитории: `@Repository`
   - Конфигурация: `@Configuration`

4. **Безопасность:**
   - Пароли хешируются через `BCryptPasswordEncoder`
   - Все защищённые маршруты требуют аутентификации
   - Статические ресурсы и `/login` публичные

### Работа с базой данных

1. **JPA сущности:**
   - Используйте `@Entity`, `@Table`, `@Id`, `@GeneratedValue`
   - Связи: `@ManyToOne`, `@OneToMany`, `@JoinColumn`
   - Lazy loading по умолчанию

2. **Репозитории:**
   - Наследуются от `JpaRepository<T, ID>`
   - Для сложных запросов используйте `@Query` с `JOIN FETCH` для избежания `LazyInitializationException`
   - Пример: `@Query("SELECT DISTINCT a FROM Account a JOIN FETCH a.system WHERE a.user.id = :userId")`

3. **Транзакции:**
   - Методы сервисов помечаются `@Transactional`
   - Изменения в БД должны быть в транзакции

### Шаблоны Thymeleaf

1. **Структура:**
   - Основной layout: `components/layout.html`
   - Наследование: `<div th:replace="~{components/layout :: head('Title')}"></div>`
   - Сайдбар: `<aside th:replace="~{components/layout :: sidebar('active-menu')}"></aside>`

2. **Локализация:**
   - Используйте `#{key}` для текстов
   - Файлы: `messages_ru.properties`, `messages_en.properties`

3. **Формы:**
   - Для create/edit используйте отдельные формы (Thymeleaf может ошибаться с условными блоками)
   - CSRF защищён (отключён в конфигурации для упрощения)

### Архитектурные решения

1. **Роли и права:**
   - Роли назначаются **пользователю**, а не аккаунту
   - `RoleAssignment` содержит ссылку на `User` и `Role`
   - Роли могут быть общими (без привязки к системе) или специфичными для системы

2. **Профилирование:**
   - `ProvisioningService` отвечает за создание/блокировку аккаунтов
   - Статусы: `PENDING_CREATE`, `SUCCESS`, `FAILED`, `BLOCKED`

3. **Workflow:**
   - `WorkflowEngineService` — центральный оркестратор событий
   - Типы событий: `HR_EVENT`, `ACCESS_REQUEST`
   - Маршрутизация в `IdentityService` или `RequestService`

---

## 📚 Ключевые компоненты

### Контроллеры

| Контроллер | Маршруты | Назначение |
|------------|----------|------------|
| `AdminController` | `/users`, `/requests`, `/audit`, `/profile` | Администрирование |
| `AccountController` | `/users/{userId}/accounts/*` | Управление аккаунтами |
| `TargetSystemController` | `/target-systems/*` | Целевые системы |
| `RoleController` | `/roles/*` | Роли |
| `IdmWebController` | `/`, `/demo/*` | Главная, демо-страницы |
| `LoginController` | `/login` | Вход/выход |

### Сервисы

| Сервис | Назначение |
|--------|------------|
| `WorkflowEngineService` | Оркестрация IDM-событий |
| `IdentityService` | Управление жизненным циклом пользователей |
| `ProvisioningService` | Профилирование аккаунтов |
| `RequestService` | Управление заявками на доступ |

### Конфигурация

- `SecurityConfig` — Spring Security (form login, BCrypt)
- `WebConfig` — настройки веб-слоя

---

## 🐛 Известные ограничения и предупреждения

### lazy initialization

- **Проблема:** `LazyInitializationException` при доступе к ленивым связям вне сессии
- **Решение:** Используйте `JOIN FETCH` в JPQL-запросах репозиториев

### Builder и начальные значения

- **Проблема:** Lombok `@Builder` игнорирует начальные значения полей
- **Решение:** Используйте `@Builder.Default` для default-значений

### Enum и типы данных

- Убедитесь, что имена enum совпадают с кодом (например, `ADMIN` вместо `ADMINISTRATIVE`)

---

## 🔄 Типичные задачи

### Добавить новую сущность

1. Создать класс в `model/` с аннотациями JPA
2. Создать репозиторий в `repository/`
3. Добавить контроллер в `controller/`
4. Создать шаблоны в `templates/`
5. Добавить переводы в `messages*.properties`

### Добавить новый маршрут

1. Добавить метод в контроллер с `@GetMapping`/`@PostMapping`
2. Добавить шаблон в `templates/`
3. Обновить навигацию в `components/layout.html`

### Исправить LazyInitializationException

1. Найти репозиторий с методом
2. Добавить `@Query` с `JOIN FETCH` для нужных связей
3. Переписать метод на использование кастомного запроса

---

## 📞 Контакты и поддержка

- **Команда:** NLP-Core-Team
- **Репозиторий:** (указать при наличии)
- **Версия KODA:** 1.0.0
- **Дата обновления:** 2026-06-14

---

## 📌 Примечания для AI-помощника

1. **Язык ответов:** Русский (по умолчанию)
2. **Код:** Всегда в markdown-блоках с указанием языка и пути
3. **Изменения:** Показывай только изменённые участки кода
4. **Тестирование:** После изменений запускай `mvn test` или `mvn compile`
5. **Архитектура:** Следуй 3-слойной модели (Controller → Service → Repository)
6. **Безопасность:** Не логируй пароли и секреты
7. **Коммиты:** Не делай commit без явного запроса
