package ru.mtkp.idm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.mtkp.idm.model.Department;
import ru.mtkp.idm.repository.DepartmentRepository;

import java.util.List;

/**
 * Контроллер управления департаментами.
 */
@Slf4j
@Controller
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    /**
     * Список всех департаментов.
     */
    @GetMapping
    public String listDepartments(Model model) {
        log.info("Получение списка всех департаментов");

        List<Department> departments = departmentRepository.findAll();
        List<Department> rootDepartments = departmentRepository.findRootDepartments();

        model.addAttribute("departments", departments);
        model.addAttribute("rootDepartments", rootDepartments);
        model.addAttribute("pageTitle", "Департаменты");
        return "departments";
    }

    /**
     * Форма создания департамента.
     */
    @GetMapping("/new")
    public String newDepartmentForm(Model model) {
        log.info("Форма создания департамента");

        List<Department> departments = departmentRepository.findAll();
        model.addAttribute("departments", departments);
        model.addAttribute("department", new Department());
        model.addAttribute("pageTitle", "Создать департамент");
        return "department-form";
    }

    /**
     * Создание департамента.
     */
    @PostMapping
    public String createDepartment(
            @RequestParam String name,
            @RequestParam String code,
            @RequestParam(required = false) Integer parentUnitId,
            RedirectAttributes redirectAttributes) {

        log.info("Создание департамента: name={}, code={}, parentUnitId={}", name, code, parentUnitId);

        try {
            Department department = new Department();
            department.setName(name);
            department.setCode(code);

            if (parentUnitId != null && parentUnitId > 0) {
                Department parent = departmentRepository.findById(parentUnitId)
                        .orElseThrow(() -> new IllegalArgumentException("Родительский департамент не найден: " + parentUnitId));
                department.setParentUnit(parent);
            }

            departmentRepository.save(department);
            redirectAttributes.addFlashAttribute("successMessage", "Департамент успешно создан");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/departments/new";
        }

        return "redirect:/departments";
    }

    /**
     * Удаление департамента.
     */
    @DeleteMapping("/{id}")
    public String deleteDepartment(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        log.info("Удаление департамента: id={}", id);

        try {
            Department department = departmentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Департамент не найден: " + id));

            // Проверка: есть ли дочерние департаменты
            if (department.getChildren() != null && !department.getChildren().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Нельзя удалить департамент с дочерними подразделениями");
                return "redirect:/departments";
            }

            // Проверка: есть ли пользователи
            if (department.getUsers() != null && !department.getUsers().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Нельзя удалить департамент с сотрудниками");
                return "redirect:/departments";
            }

            departmentRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Департамент успешно удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/departments";
    }
}