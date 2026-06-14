package ru.mtkp.idm.controller;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.mtkp.idm.model.Account;
import ru.mtkp.idm.model.AccountStatus;
import ru.mtkp.idm.model.Role;
import ru.mtkp.idm.model.RoleAssignment;
import ru.mtkp.idm.model.TargetSystem;
import ru.mtkp.idm.model.User;
import ru.mtkp.idm.repository.AccountRepository;
import ru.mtkp.idm.repository.RoleAssignmentRepository;
import ru.mtkp.idm.repository.RoleRepository;
import ru.mtkp.idm.repository.TargetSystemRepository;
import ru.mtkp.idm.repository.UserRepository;
import ru.mtkp.idm.service.ProvisioningService;

/**
 * Контроллер управления учётными записями.
 */
@Slf4j
@Controller
@RequestMapping("/users/{userId}/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TargetSystemRepository targetSystemRepository;
    private final RoleRepository roleRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final ProvisioningService provisioningService;

    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Список всех аккаунтов пользователя.
     */
    @GetMapping
    public String listUserAccounts(@PathVariable Integer userId, Model model) {
        User user = userRepository.findById(userId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));

        List<Account> accounts = accountRepository.findByUserId(userId.longValue());

        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("pageTitle", "Аккаунты пользователя: " + user.getLogin());
        return "user-accounts";
    }

    /**
     * Форма создания аккаунта.
     */
    @GetMapping("/new")
    public String newAccountForm(@PathVariable Integer userId, Model model) {
        User user = userRepository.findById(userId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));

        List<TargetSystem> systems = targetSystemRepository.findAll();
        List<Role> roles = roleRepository.findAll();

        model.addAttribute("user", user);
        model.addAttribute("systems", systems);
        model.addAttribute("roles", roles);
        model.addAttribute("pageTitle", "Добавить аккаунт");
        return "account-form";
    }

    /**
     * Создание аккаунта вручную.
     */
    @PostMapping("/create")
    public String createAccount(
            @RequestParam Integer userId,
            @RequestParam Integer systemId,
            @RequestParam String accountLogin,
            @RequestParam(required = false) Integer roleId,
            RedirectAttributes redirectAttributes) {

        log.info("Создание аккаунта вручную: userId={}, systemId={}, accountLogin={}, roleId={}",
                userId, systemId, accountLogin, roleId);

        User user = userRepository.findById(userId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));

        ru.mtkp.idm.model.TargetSystem system = targetSystemRepository.findById(systemId)
                .orElseThrow(() -> new IllegalArgumentException("Система не найдена: " + systemId));

        // Проверяем, нет ли уже аккаунта
        if (accountRepository.findByUserIdAndSystemId(userId.longValue(), systemId).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Аккаунт в этой системе уже существует!");
            return "redirect:/users/" + userId + "/accounts/new";
        }

        // Создаём аккаунт
        Account account = Account.builder()
                .user(user)
                .system(system)
                .accountLogin(accountLogin)
                .status(AccountStatus.ACTIVE)
                .provisioningStatus(ru.mtkp.idm.model.ProvisioningStatus.SUCCESS)
                .build();

        accountRepository.save(account);

        // Если выбрана роль, назначаем её
        if (roleId != null && roleId > 0) {
            ru.mtkp.idm.model.Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleId));

            ru.mtkp.idm.model.RoleAssignment assignment = ru.mtkp.idm.model.RoleAssignment.builder()
                    .user(user)
                    .role(role)
                    .assignmentType(ru.mtkp.idm.model.AssignType.DIRECT)
                    .build();

            roleAssignmentRepository.save(assignment);
            log.info("Роль {} назначена пользователю", role.getName());
        }

        redirectAttributes.addFlashAttribute("successMessage",
            "Аккаунт \"" + accountLogin + "\" успешно создан в системе \"" + system.getName() + "\"");
        return "redirect:/users/" + userId + "/accounts";
    }

    /**
     * Детальный просмотр аккаунта.
     */
    @GetMapping("/{accountId}")
    public String viewAccount(@PathVariable Integer userId, @PathVariable Integer accountId, Model model) {
        Account account = accountRepository.findByIdWithUserAndSystem(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Аккаунт не найден: " + accountId));

        if (!account.getUser().getId().equals(userId.longValue())) {
            throw new IllegalArgumentException("Аккаунт не принадлежит пользователю");
        }

        // Загружаем назначения ролей для этого пользователя
        List<RoleAssignment> assignments = roleAssignmentRepository.findByUserId(userId.longValue());

        model.addAttribute("account", account);
        model.addAttribute("assignments", assignments);
        model.addAttribute("pageTitle", "Аккаунт: " + account.getAccountLogin());
        return "account-detail";
    }

    /**
     * Блокировка аккаунта.
     */
    @PostMapping("/{accountId}/block")
    public String blockAccount(@PathVariable Integer userId, @PathVariable Integer accountId, RedirectAttributes redirectAttributes) {
        log.info("Блокировка аккаунта: accountId={}, userId={}", accountId, userId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Аккаунт не найден: " + accountId));

        if (!account.getUser().getId().equals(userId.longValue())) {
            throw new IllegalArgumentException("Аккаунт не принадлежит пользователю");
        }

        User user = account.getUser();
        provisioningService.blockAccount(user, account.getSystem().getName());

        redirectAttributes.addFlashAttribute("successMessage", "Аккаунт \"" + account.getAccountLogin() + "\" заблокирован");
        return "redirect:/users/" + userId + "/accounts";
    }

    /**
     * Разблокировка аккаунта.
     */
    @PostMapping("/{accountId}/unblock")
    public String unblockAccount(@PathVariable Integer userId, @PathVariable Integer accountId, RedirectAttributes redirectAttributes) {
        log.info("Разблокировка аккаунта: accountId={}, userId={}", accountId, userId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Аккаунт не найден: " + accountId));

        if (!account.getUser().getId().equals(userId.longValue())) {
            throw new IllegalArgumentException("Аккаунт не принадлежит пользователю");
        }

        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        redirectAttributes.addFlashAttribute("successMessage", "Аккаунт \"" + account.getAccountLogin() + "\" разблокирован");
        return "redirect:/users/" + userId + "/accounts";
    }

    /**
     * Сброс пароля аккаунта.
     */
    @PostMapping("/{accountId}/reset-password")
    public String resetPassword(@PathVariable Integer userId, @PathVariable Integer accountId, RedirectAttributes redirectAttributes) {
        log.info("Сброс пароля аккаунта: accountId={}, userId={}", accountId, userId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Аккаунт не найден: " + accountId));

        if (!account.getUser().getId().equals(userId.longValue())) {
            throw new IllegalArgumentException("Аккаунт не принадлежит пользователю");
        }

        // Генерируем случайный пароль
        String newPassword = generatePassword();

        log.info("Сгенерирован новый пароль для аккаунта {}: {}", accountId, "****");

        // В реальном проекте здесь был бы вызов к системе для установки пароля
        // Для MVP просто логируем
        redirectAttributes.addFlashAttribute("successMessage",
            "Пароль сброшен. Новый пароль: " + newPassword);
        return "redirect:/users/" + userId + "/accounts/" + accountId;
    }

    /**
     * Удаление аккаунта.
     */
    @PostMapping("/{accountId}/delete")
    public String deleteAccount(@PathVariable Integer userId, @PathVariable Integer accountId, RedirectAttributes redirectAttributes) {
        log.info("Удаление аккаунта: accountId={}, userId={}", accountId, userId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Аккаунт не найден: " + accountId));

        if (!account.getUser().getId().equals(userId.longValue())) {
            throw new IllegalArgumentException("Аккаунт не принадлежит пользователю");
        }

        String accountLogin = account.getAccountLogin();
        accountRepository.delete(account);

        redirectAttributes.addFlashAttribute("successMessage", "Аккаунт \"" + accountLogin + "\" удалён");
        return "redirect:/users/" + userId + "/accounts";
    }

    /**
     * Отзыв всех ролей для аккаунта.
     */
    @PostMapping("/{accountId}/revoke-all-roles")
    public String revokeAllRoles(@PathVariable Integer userId, @PathVariable Integer accountId, RedirectAttributes redirectAttributes) {
        log.info("Отзыв всех ролей для аккаунта: accountId={}, userId={}", accountId, userId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Аккаунт не найден: " + accountId));

        if (!account.getUser().getId().equals(userId.longValue())) {
            throw new IllegalArgumentException("Аккаунт не принадлежит пользователю");
        }

        // Отзываем все активные назначения ролей для пользователя
        List<RoleAssignment> assignments = roleAssignmentRepository.findByUserId(userId.longValue());
        int revokedCount = 0;
        for (RoleAssignment assignment : assignments) {
            assignment.setEffectiveTo(LocalDate.now().minusDays(1));
            roleAssignmentRepository.save(assignment);
            revokedCount++;
        }

        redirectAttributes.addFlashAttribute("successMessage",
            "Отозвано " + revokedCount + " назначений ролей");
        return "redirect:/users/" + userId + "/accounts/" + accountId;
    }

    /**
     * Генерация случайного пароля.
     */
    private String generatePassword() {
        int length = 16;
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(PASSWORD_CHARS.charAt(secureRandom.nextInt(PASSWORD_CHARS.length())));
        }
        return password.toString();
    }
}