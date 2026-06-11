package ru.mtkp.idm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import ru.mtkp.idm.repository.UserRepository;
import ru.mtkp.idm.repository.RequestRepository;

/**
 * Контроллер для административных страниц: Users, Requests, Audit, Profile.
 */
@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;

    public AdminController(UserRepository userRepository, RequestRepository requestRepository) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("active", "users");
        return "users";
    }

    @GetMapping("/requests")
    public String requests(Model model) {
        model.addAttribute("requests", requestRepository.findAll());
        model.addAttribute("active", "requests");
        return "requests";
    }

    @GetMapping("/audit")
    public String audit(Model model) {
        model.addAttribute("events", requestRepository.findAll());
        model.addAttribute("active", "audit");
        return "audit";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        var user = userRepository.findAll().stream().findFirst().orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("active", "profile");
        return "profile";
    }

    @PostMapping("/users/{id}/suspend")
    public String suspendUserPost(@PathVariable Long id) {
        return "redirect:/users";
    }

    @PostMapping("/users/{id}/terminate")
    public String terminateUserPost(@PathVariable Long id) {
        return "redirect:/users";
    }
}