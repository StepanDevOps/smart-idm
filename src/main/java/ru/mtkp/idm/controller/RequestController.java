package ru.mtkp.idm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.mtkp.idm.model.Request;
import ru.mtkp.idm.model.Role;
import ru.mtkp.idm.model.TargetSystem;
import ru.mtkp.idm.model.User;
import ru.mtkp.idm.repository.RoleRepository;
import ru.mtkp.idm.repository.TargetSystemRepository;
import ru.mtkp.idm.repository.UserRepository;
import ru.mtkp.idm.service.RequestService;

import java.security.Principal;
import java.util.List;

/**
 * Контроллер управления заявками на доступ.
 */
@Slf4j
@Controller
@RequestMapping("/access-requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TargetSystemRepository targetSystemRepository;

    /**
     * Форма создания заявки.
     */
    @GetMapping("/new")
    public String newRequestForm(Model model, Principal principal) {
        log.info("Форма создания заявки на доступ");

        List<User> users = userRepository.findAll();
        List<TargetSystem> systems = targetSystemRepository.findAll();
        List<Role> roles = roleRepository.findAll();

        // Текущий пользователь (инициатор)
        User requestor = null;
        if (principal != null) {
            requestor = userRepository.findAll().stream()
                    .filter(u -> u.getLogin().equals(principal.getName()))
                    .findFirst()
                    .orElse(null);
        }

        model.addAttribute("users", users);
        model.addAttribute("systems", systems);
        model.addAttribute("roles", roles);
        model.addAttribute("requestor", requestor);
        model.addAttribute("pageTitle", "Создать заявку на доступ");
        return "access-request-form";
    }

    /**
     * Создание заявки.
     */
    @PostMapping("/create")
    public String createRequest(
            @RequestParam Long requestorId,
            @RequestParam Long requestedForId,
            @RequestParam Integer roleId,
            @RequestParam(required = false) String targetSystem,
            @RequestParam String justification,
            RedirectAttributes redirectAttributes) {

        log.info("Создание заявки: requestorId={}, requestedForId={}, roleId={}, system={}, justification={}",
                requestorId, requestedForId, roleId, targetSystem, justification);

        try {
            User requestor = userRepository.findById(requestorId)
                    .orElseThrow(() -> new IllegalArgumentException("Инициатор не найден: " + requestorId));

            User requestedFor = userRepository.findById(requestedForId)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + requestedForId));

            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleId));

            Request request = requestService.createAccessRequest(
                    requestor, requestedFor, role.getName(),
                    targetSystem != null ? targetSystem : "DEFAULT",
                    justification);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Заявка #" + request.getId() + " успешно создана и отправлена на согласование");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/access-requests/new";
        }

        return "redirect:/requests";
    }
}
