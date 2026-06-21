package ru.mtkp.idm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.mtkp.idm.model.Department;
import ru.mtkp.idm.model.DepartmentRole;
import ru.mtkp.idm.model.Role;
import ru.mtkp.idm.repository.DepartmentRepository;
import ru.mtkp.idm.repository.RoleRepository;
import ru.mtkp.idm.service.AuditService;
import ru.mtkp.idm.service.DepartmentRoleService;

import java.util.List;

/**
 * Контроллер управления связями Department ↔ Role.
 */
@Slf4j
@Controller
@RequestMapping("/department-roles")
@RequiredArgsConstructor
public class DepartmentRoleController {

    private final DepartmentRoleService departmentRoleService;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final AuditService auditService;

    /**
     * Список всех связей Department↔Role.
     */
    @GetMapping
    public String listDepartmentRoles(Model model) {
        log.info("Получение списка всех связей Department↔Role");

        List<DepartmentRole> all = departmentRoleService.findAll();

        model.addAttribute("departmentRoles", all);
        model.addAttribute("pageTitle", "Связи департаментов с ролями");
        return "department-roles";
    }

    /**
     * Форма создания связи.
     */
    @GetMapping("/new")
    public String newForm(Model model) {
        log.info("Форма создания связи Department↔Role");

        List<Department> departments = departmentRepository.findAll();
        List<Role> roles = roleRepository.findAll();

        model.addAttribute("departments", departments);
        model.addAttribute("roles", roles);
        model.addAttribute("pageTitle", "Создать связь департамента с ролью");
        return "department-role-form";
    }

    /**
     * Создание связи.
     */
    @PostMapping
    public String createAssignment(
            @RequestParam Integer departmentId,
            @RequestParam Integer roleId,
            RedirectAttributes redirectAttributes) {

        log.info("Создание связи: departmentId={}, roleId={}", departmentId, roleId);

        try {
            departmentRoleService.createAssignment(departmentId, roleId);

            // Получаем информацию для аудита
            Department dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Департамент не найден: " + departmentId));
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleId));

            auditService.logAction(null, "INDIRECT_ASSIGNMENT_CREATED",
                    "INDIRECT роль " + role.getName() + " назначена для департамента " + dept.getName());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Связь успешно создана: департамент " + departmentId + ", роль " + roleId);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/department-roles/new";
        }

        return "redirect:/department-roles";
    }

    /**
     * Удаление связи.
     */
    @DeleteMapping("/{id}")
    public String deleteAssignment(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        log.info("Удаление связи: id={}", id);

        try {
            // Получаем информацию для аудита перед удалением
            DepartmentRole dr = departmentRoleService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Связь не найдена: " + id));

            String deptName = dr.getDepartment() != null ? dr.getDepartment().getName() : "unknown";
            String roleName = dr.getRole() != null ? dr.getRole().getName() : "unknown";

            boolean deleted = departmentRoleService.deleteAssignment(id);
            if (deleted) {
                auditService.logAction(null, "INDIRECT_ASSIGNMENT_DELETED",
                        "Удалена INDIRECT связь: департамент " + deptName + ", роль " + roleName);
                redirectAttributes.addFlashAttribute("successMessage", "Связь успешно удалена");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Связь не найдена");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/department-roles";
    }

    /**
     * AJAX: получить все роли для департамента (с учётом детей).
     */
    @GetMapping("/roles/{departmentId}")
    @ResponseBody
    public List<Integer> getRolesForDepartment(@PathVariable Integer departmentId) {
        log.info("Получение ролей для департамента: {}", departmentId);
        return departmentRoleService.getAllRolesForDepartmentWithChildren(departmentId);
    }

    /**
     * AJAX: получить всех детей департамента.
     */
    @GetMapping("/children/{departmentId}")
    @ResponseBody
    public List<Department> getChildren(@PathVariable Integer departmentId) {
        Department dept = departmentRepository.findByIdWithChildren(departmentId);
        if (dept == null) {
            throw new IllegalArgumentException("Департамент не найден: " + departmentId);
        }
        return dept.getChildren() != null ? dept.getChildren() : List.of();
    }
}