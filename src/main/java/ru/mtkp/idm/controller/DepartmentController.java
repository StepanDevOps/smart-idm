package ru.mtkp.idm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.mtkp.idm.model.Department;
import ru.mtkp.idm.repository.DepartmentRepository;
import ru.mtkp.idm.service.AuditService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер управления департаментами.
 */
@Slf4j
@Controller
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;
    private final AuditService auditService;

    /**
     * Список всех департаментов.
     */
    @Transactional(readOnly = true)
    @GetMapping
    public String listDepartments(Model model) {
        log.info("Получение списка всех департаментов");

        List<Department> departments = departmentRepository.findAll();
        
        // Добавляем userCount для каждого департамента (чтобы избежать lazy loading)
        List<Map<String, Object>> departmentsWithCount = new ArrayList<>();
        for (Department dept : departments) {
            Map<String, Object> deptData = new HashMap<>();
            deptData.put("id", dept.getId());
            deptData.put("name", dept.getName());
            deptData.put("code", dept.getCode());
            deptData.put("parentUnit", dept.getParentUnit());
            deptData.put("hasChildren", dept.getChildren() != null && !dept.getChildren().isEmpty());
            deptData.put("userCount", dept.getUsers() != null ? dept.getUsers().size() : 0);
            departmentsWithCount.add(deptData);
        }

        model.addAttribute("departments", departmentsWithCount);
        model.addAttribute("pageTitle", "Departments");
        return "departments";
    }

    /**
     * Форма создания департамента.
     */
    @Transactional(readOnly = true)
    @GetMapping("/new")
    public String newDepartmentForm(Model model) {
        log.info("Форма создания департамента");

        List<Department> departments = departmentRepository.findAll();
        model.addAttribute("departments", departments);
        model.addAttribute("department", new Department());
        model.addAttribute("pageTitle", "Create Department");
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
                        .orElseThrow(() -> new IllegalArgumentException("Parent department not found: " + parentUnitId));
                department.setParentUnit(parent);
            }

            departmentRepository.save(department);

            auditService.logAction(null, "DEPARTMENT_CREATED",
                    "Создан департамент: " + name + " (код: " + code + ")");

            redirectAttributes.addFlashAttribute("successMessage", "Department created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/departments/new";
        }

        return "redirect:/departments";
    }

    /**
     * Удаление департамента.
     */
    @Transactional
    @DeleteMapping("/{id}")
    public String deleteDepartment(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        log.info("Удаление департамента: id={}", id);

        try {
            Department department = departmentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found: " + id));

            // Проверка: есть ли дочерние департаменты
            if (department.getChildren() != null && !department.getChildren().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete department with child departments");
                return "redirect:/departments";
            }

            // Проверка: есть ли пользователи
            if (department.getUsers() != null && !department.getUsers().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete department with users");
                return "redirect:/departments";
            }

            departmentRepository.deleteById(id);

            auditService.logAction(null, "DEPARTMENT_DELETED",
                    "Удалён департамент: " + department.getName() + " (код: " + department.getCode() + ")");

            redirectAttributes.addFlashAttribute("successMessage", "Department deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/departments";
    }
}