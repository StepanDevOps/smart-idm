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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.mtkp.idm.model.Role;
import ru.mtkp.idm.model.RoleType;
import ru.mtkp.idm.model.TargetSystem;
import ru.mtkp.idm.repository.RoleRepository;
import ru.mtkp.idm.repository.TargetSystemRepository;
import ru.mtkp.idm.service.AuditService;

/**
 * Контроллер управления ролями.
 */
@Slf4j
@Controller
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleRepository roleRepository;
    private final TargetSystemRepository targetSystemRepository;
    private final AuditService auditService;

    /**
     * Список всех ролей.
     */
    @GetMapping
    public String listRoles(
            @RequestParam(required = false) Integer systemId,
            Model model) {

        List<Role> roles;
        if (systemId != null) {
            roles = roleRepository.findBySystemId(systemId);
            TargetSystem system = targetSystemRepository.findById(systemId).orElse(null);
            model.addAttribute("selectedSystem", system);
        } else {
            roles = roleRepository.findAll();
        }

        List<TargetSystem> systems = targetSystemRepository.findAll();

        model.addAttribute("roles", roles);
        model.addAttribute("systems", systems);
        model.addAttribute("pageTitle", "Роли");
        model.addAttribute("systemId", systemId);
        return "roles";
    }

    /**
     * Форма создания роли.
     */
    @GetMapping("/new")
    public String newRoleForm(Model model) {
        model.addAttribute("role", new Role());
        model.addAttribute("pageTitle", "Добавить роль");
        model.addAttribute("isEdit", false);

        List<TargetSystem> systems = targetSystemRepository.findAll();
        model.addAttribute("systems", systems);

        return "role-form";
    }

    /**
     * Создание роли.
     */
    @PostMapping("/create")
    public String createRole(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer systemId,
            @RequestParam(required = false) ru.mtkp.idm.model.RoleType roleType,
            @RequestParam(required = false) Boolean isSensitive,
            RedirectAttributes redirectAttributes) {

        log.info("Создание роли: name={}, description={}, systemId={}, roleType={}, isSensitive={}",
                name, description, systemId, roleType, isSensitive);

        Role role = Role.builder()
                .name(name)
                .description(description)
                .roleType(roleType != null ? roleType : RoleType.BUSINESS)
                .isSensitive(isSensitive != null && isSensitive)
                .build();

        if (systemId != null) {
            TargetSystem system = targetSystemRepository.findById(systemId).orElse(null);
            role.setSystem(system);
        }

        roleRepository.save(role);

        auditService.logAction(null, "ROLE_CREATED",
                "Создана роль: " + name + (systemId != null ? " (система ID=" + systemId + ")" : " (глобальная)"));

        redirectAttributes.addFlashAttribute("successMessage", "Роль \"" + name + "\" успешно создана");
        return "redirect:/roles";
    }

    /**
     * Форма редактирования роли.
     */
    @GetMapping("/edit/{id}")
    public String editRoleForm(@PathVariable Integer id, Model model) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + id));

        model.addAttribute("role", role);
        model.addAttribute("pageTitle", "Редактировать роль");
        model.addAttribute("isEdit", true);

        List<TargetSystem> systems = targetSystemRepository.findAll();
        model.addAttribute("systems", systems);

        return "role-form";
    }

    /**
     * Обновление роли.
     */
    @PostMapping("/update/{id}")
    public String updateRole(
            @PathVariable Integer id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer systemId,
            @RequestParam(required = false) ru.mtkp.idm.model.RoleType roleType,
            @RequestParam(required = false) Boolean isSensitive,
            RedirectAttributes redirectAttributes) {

        log.info("Обновление роли: id={}, name={}, description={}, systemId={}, roleType={}, isSensitive={}",
                id, name, description, systemId, roleType, isSensitive);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + id));

        role.setName(name);
        role.setDescription(description);
        if (roleType != null) {
            role.setRoleType(roleType);
        }
        role.setIsSensitive(isSensitive != null && isSensitive);

        if (systemId != null) {
            TargetSystem system = targetSystemRepository.findById(systemId).orElse(null);
            role.setSystem(system);
        } else {
            role.setSystem(null);
        }

        roleRepository.save(role);

        redirectAttributes.addFlashAttribute("successMessage", "Роль \"" + name + "\" успешно обновлена");
        return "redirect:/roles";
    }

    /**
     * Удаление роли.
     */
    @PostMapping("/delete/{id}")
    public String deleteRole(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        log.info("Удаление роли: id={}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + id));

        String roleName = role.getName();

        auditService.logAction(null, "ROLE_DELETED",
                "Удалена роль: " + roleName);

        roleRepository.delete(role);

        redirectAttributes.addFlashAttribute("successMessage", "Роль \"" + roleName + "\" успешно удалена");
        return "redirect:/roles";
    }
}