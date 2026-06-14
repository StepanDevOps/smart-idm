package ru.mtkp.idm.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.mtkp.idm.model.Account;
import ru.mtkp.idm.model.AccountStatus;
import ru.mtkp.idm.model.User;
import ru.mtkp.idm.repository.AccountRepository;
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
    private final ProvisioningService provisioningService;

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
}