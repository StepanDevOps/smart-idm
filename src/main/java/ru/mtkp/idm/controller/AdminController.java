package ru.mtkp.idm.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ru.mtkp.idm.model.Account;
import ru.mtkp.idm.model.RoleAssignment;
import ru.mtkp.idm.model.SecurityLog;
import ru.mtkp.idm.model.User;
import ru.mtkp.idm.model.UserStatus;
import ru.mtkp.idm.repository.AccountRepository;
import ru.mtkp.idm.repository.RequestRepository;
import ru.mtkp.idm.repository.RoleAssignmentRepository;
import ru.mtkp.idm.repository.SecurityLogRepository;
import ru.mtkp.idm.repository.UserRepository;

/**
 * Контроллер для административных страниц: Users, Requests, Audit, Profile.
 */
@Slf4j
@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final AccountRepository accountRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final SecurityLogRepository securityLogRepository;

    public AdminController(UserRepository userRepository, RequestRepository requestRepository,
                          AccountRepository accountRepository, RoleAssignmentRepository roleAssignmentRepository,
                          SecurityLogRepository securityLogRepository) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.accountRepository = accountRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.securityLogRepository = securityLogRepository;
    }

    /**
     * Список пользователей (администрирование идентичностей).
     */
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("active", "users");
        return "users";
    }

    /**
     * Портал самообслуживания: список заявок текущего пользователя (демо показывает все заявки).
     */
    @GetMapping("/requests")
    public String requests(Model model) {
        model.addAttribute("requests", requestRepository.findAll());
        model.addAttribute("active", "requests");
        return "requests";
    }

    /**
     * Журнал аудита — список событий SecurityLog с фильтрами и пагинацией.
     */
    @GetMapping("/audit")
    public String audit(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        log.info("Получение журнала аудита: eventType={}, userId={}, fromDate={}, toDate={}, page={}, size={}",
                eventType, userId, fromDate, toDate, page, size);

        // Парсим даты
        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (fromDate != null && !fromDate.isEmpty()) {
            try {
                fromDateTime = LocalDate.parse(fromDate).atStartOfDay();
            } catch (DateTimeParseException e) {
                model.addAttribute("errorMessage", "Неверный формат даты начала");
            }
        }

        if (toDate != null && !toDate.isEmpty()) {
            try {
                toDateTime = LocalDate.parse(toDate).atTime(LocalTime.MAX);
            } catch (DateTimeParseException e) {
                model.addAttribute("errorMessage", "Неверный формат даты окончания");
            }
        }

        // Создаём пагинацию
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eventTime"));

        // Получаем логи с фильтрами
        Page<SecurityLog> logPage;
        if (eventType == null && userId == null && fromDateTime == null && toDateTime == null) {
            // Без фильтров
            logPage = securityLogRepository.findAllByOrderByEventTimeDesc(pageable);
        } else {
            // С фильтрами - используем дефолтные значения для null дат
            LocalDateTime filterFrom = fromDateTime != null ? fromDateTime : LocalDateTime.of(1970, 1, 1, 0, 0);
            LocalDateTime filterTo = toDateTime != null ? toDateTime : LocalDateTime.of(2099, 12, 31, 23, 59);

            logPage = securityLogRepository.findByEventTypeAndUserIdAndEventTimeBetween(
                    eventType, userId, filterFrom, filterTo, pageable);
        }

        // Получаем списки для фильтров
        List<String> eventTypes = securityLogRepository.findDistinctEventTypes();
        List<User> users = userRepository.findAll();

        model.addAttribute("logs", logPage);
        model.addAttribute("eventTypes", eventTypes);
        model.addAttribute("users", users);
        model.addAttribute("selectedEventType", eventType);
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("active", "audit");
        model.addAttribute("pageTitle", "Журнал аудита");

        return "audit";
    }

    /**
     * Личный кабинет пользователя (показываем информацию о первом пользователе как демо).
     */
    @GetMapping("/profile")
    public String profile(Model model) {
        var user = userRepository.findAll().stream().findFirst().orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("active", "profile");
        return "profile";
    }

	/**
	 * Приостановить пользователя (soft suspend).
	 */
	@PostMapping("/users/{id}/suspend")
	public String suspendUserPost(@PathVariable Long id) {
		userRepository.findById(id).ifPresent(user -> {
			user.setStatus(UserStatus.SUSPENDED);
			userRepository.save(user);
		});
		return "redirect:/users";
	}

	/**
	 * Уволить пользователя (soft delete).
	 */
	@PostMapping("/users/{id}/terminate")
	public String terminateUserPost(@PathVariable Long id) {
		userRepository.findById(id).ifPresent(user -> {
			user.setStatus(UserStatus.TERMINATED);
			userRepository.save(user);
		});
		return "redirect:/users";
	}

	/**
	 * Детальный просмотр пользователя.
	 */
	@GetMapping("/users/{id}")
	public String viewUser(@PathVariable Long id, Model model) {
		var user = userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));

		List<Account> accounts = accountRepository.findByUserId(id);
		List<RoleAssignment> assignments = roleAssignmentRepository.findByUserId(id);

		model.addAttribute("user", user);
		model.addAttribute("accounts", accounts);
		model.addAttribute("assignments", assignments);
		return "user-detail";
	}
}