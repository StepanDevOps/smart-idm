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

import ru.mtkp.idm.model.TargetSystem;
import ru.mtkp.idm.repository.TargetSystemRepository;

/**
 * Контроллер управления целевыми системами.
 */
@Slf4j
@Controller
@RequestMapping("/target-systems")
@RequiredArgsConstructor
public class TargetSystemController {

    private final TargetSystemRepository targetSystemRepository;

    /**
     * Список всех целевых систем.
     */
    @GetMapping
    public String listSystems(Model model) {
        List<TargetSystem> systems = targetSystemRepository.findAll();
        model.addAttribute("systems", systems);
        model.addAttribute("pageTitle", "Целевые системы");
        return "target-systems";
    }

    /**
     * Форма создания системы.
     */
    @GetMapping("/new")
    public String newSystemForm(Model model) {
        model.addAttribute("targetSystem", new TargetSystem());
        model.addAttribute("pageTitle", "Добавить целевую систему");
        model.addAttribute("isEdit", false);
        return "target-system-form";
    }

    /**
     * Создание системы.
     */
    @PostMapping("/create")
    public String createSystem(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) ru.mtkp.idm.model.SystemType type,
            RedirectAttributes redirectAttributes) {

        log.info("Создание целевой системы: name={}, description={}, type={}", name, description, type);

        TargetSystem system = TargetSystem.builder()
                .name(name)
                .description(description)
                .type(type)
                .build();

        targetSystemRepository.save(system);

        redirectAttributes.addFlashAttribute("successMessage", "Система \"" + name + "\" успешно создана");
        return "redirect:/target-systems";
    }

    /**
     * Форма редактирования системы.
     */
    @GetMapping("/edit/{id}")
    public String editSystemForm(@PathVariable Integer id, Model model) {
        log.info("Загрузка формы редактирования системы: id={}", id);

        TargetSystem system = targetSystemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Система не найдена: " + id));

        log.info("Найдена система: id={}, name={}", system.getId(), system.getName());

        model.addAttribute("targetSystem", system);
        model.addAttribute("pageTitle", "Редактировать систему");
        model.addAttribute("isEdit", true);
        return "target-system-form";
    }

    /**
     * Обновление системы.
     */
    @PostMapping("/update/{id}")
    public String updateSystem(
            @PathVariable Integer id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) ru.mtkp.idm.model.SystemType type,
            RedirectAttributes redirectAttributes) {

        log.info("Обновление целевой системы: id={}, name={}, description={}, type={}", id, name, description, type);

        TargetSystem system = targetSystemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Система не найдена: " + id));

        system.setName(name);
        system.setDescription(description);
        if (type != null) {
            system.setType(type);
        }

        targetSystemRepository.save(system);

        redirectAttributes.addFlashAttribute("successMessage", "Система \"" + name + "\" успешно обновлена");
        return "redirect:/target-systems";
    }

    /**
     * Удаление системы.
     */
    @PostMapping("/delete/{id}")
    public String deleteSystem(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        log.info("Удаление целевой системы: id={}", id);

        TargetSystem system = targetSystemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Система не найдена: " + id));

        String systemName = system.getName();
        targetSystemRepository.delete(system);

        redirectAttributes.addFlashAttribute("successMessage", "Система \"" + systemName + "\" успешно удалена");
        return "redirect:/target-systems";
    }

    /**
     * Просмотр системы и её аккаунтов.
     */
    @GetMapping("/{id}")
    public String viewSystem(@PathVariable Integer id, Model model) {
        // Загружаем систему без коллекций
        TargetSystem system = targetSystemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Система не найдена: " + id));

        // Загружаем роли и аккаунты отдельно
        List<ru.mtkp.idm.model.Role> roles = targetSystemRepository.findRolesBySystemId(id);
        List<ru.mtkp.idm.model.Account> accounts = targetSystemRepository.findAccountsBySystemId(id);

        model.addAttribute("system", system);
        model.addAttribute("roles", roles);
        model.addAttribute("accounts", accounts);
        model.addAttribute("pageTitle", "Система: " + system.getName());
        return "target-system-detail";
    }
}


