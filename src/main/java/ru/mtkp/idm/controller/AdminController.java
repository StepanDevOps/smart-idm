package ru.mtkp.idm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ru.mtkp.idm.repository.UserRepository;
import ru.mtkp.idm.repository.IdmRequestRepository;

/**
 * Контроллер для административных страниц: Users, Requests, Audit, Profile.
 */
@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final IdmRequestRepository idmRequestRepository;

    public AdminController(UserRepository userRepository, IdmRequestRepository idmRequestRepository) {
        this.userRepository = userRepository;
        this.idmRequestRepository = idmRequestRepository;
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
        model.addAttribute("requests", idmRequestRepository.findAll());
        model.addAttribute("active", "requests");
        return "requests";
    }

    /**
     * Журнал аудита — список событий (демо: используем заявки как записи).
     */
    @GetMapping("/audit")
    public String audit(Model model) {
        model.addAttribute("events", idmRequestRepository.findAll());
        model.addAttribute("active", "audit");
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
     * Одобрить заявку (demo).
     */
    @org.springframework.web.bind.annotation.PostMapping("/requests/{id}/approve")
    public String approveRequestPost(@org.springframework.web.bind.annotation.PathVariable Long id) {
        // TODO: логика approve — для MVP помечаем как обработано в репозитории
        return "redirect:/requests";
    }

    @org.springframework.web.bind.annotation.PostMapping("/requests/{id}/reject")
    public String rejectRequestPost(@org.springframework.web.bind.annotation.PathVariable Long id) {
        // TODO: логика reject
        return "redirect:/requests";
    }

    // Пример действий над пользователем
    @org.springframework.web.bind.annotation.PostMapping("/users/{id}/suspend")
    public String suspendUserPost(@org.springframework.web.bind.annotation.PathVariable Long id) {
        // TODO: пометить пользователя как приостановленный
        return "redirect:/users";
    }

    @org.springframework.web.bind.annotation.PostMapping("/users/{id}/terminate")
    public String terminateUserPost(@org.springframework.web.bind.annotation.PathVariable Long id) {
        // TODO: выполнить процедуру увольнения
        return "redirect:/users";
    }
}



