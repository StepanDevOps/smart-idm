package ru.mtkp.idm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mtkp.idm.model.Department;
import ru.mtkp.idm.model.DepartmentRole;
import ru.mtkp.idm.model.Role;
import ru.mtkp.idm.repository.DepartmentRepository;
import ru.mtkp.idm.repository.DepartmentRoleRepository;
import ru.mtkp.idm.repository.RoleRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для управления связями Department ↔ Role.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentRoleServiceImpl implements ru.mtkp.idm.service.DepartmentRoleService {

    private final DepartmentRoleRepository departmentRoleRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;

    @Override
    public List<DepartmentRole> findAll() {
        return departmentRoleRepository.findAll();
    }

    @Override
    public DepartmentRole createAssignment(Integer departmentId, Integer roleId) {
        // Проверка существования департамента
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Департамент не найден: " + departmentId));

        // Проверка существования роли
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleId));

        // Проверка на дубликат
        if (departmentRoleRepository.findByDepartmentIdAndRoleId(departmentId, roleId).isPresent()) {
            throw new IllegalArgumentException("Связь уже существует: департамент " + departmentId + ", роль " + roleId);
        }

        DepartmentRole departmentRole = DepartmentRole.builder()
                .department(department)
                .role(role)
                .build();

        DepartmentRole saved = departmentRoleRepository.save(departmentRole);
        log.info("Создана связь Department↔Role: departmentId={}, roleId={}", departmentId, roleId);
        return saved;
    }

    @Override
    public boolean deleteAssignment(Integer id) {
        if (!departmentRoleRepository.existsById(id)) {
            log.warn("Связь не найдена для удаления: id={}", id);
            return false;
        }
        departmentRoleRepository.deleteById(id);
        log.info("Удалена связь Department↔Role: id={}", id);
        return true;
    }

    @Override
    public void deleteByDepartmentId(Integer departmentId) {
        departmentRoleRepository.deleteByDepartmentId(departmentId);
        log.info("Удалены все связи для департамента: {}", departmentId);
    }

    @Override
    public List<DepartmentRole> findByDepartmentId(Integer departmentId) {
        return departmentRoleRepository.findByDepartmentId(departmentId);
    }

    @Override
    public List<DepartmentRole> findByRoleId(Integer roleId) {
        return departmentRoleRepository.findByRoleId(roleId);
    }

    @Override
    public Optional<DepartmentRole> findById(Integer id) {
        return departmentRoleRepository.findById(id);
    }

    @Override
    public boolean existsByDepartmentIdAndRoleId(Integer departmentId, Integer roleId) {
        return departmentRoleRepository.findByDepartmentIdAndRoleId(departmentId, roleId).isPresent();
    }

    @Override
    public List<Integer> getAllRolesForDepartmentWithChildren(Integer departmentId) {
        // Получаем департамент с детьми
        Department department = departmentRepository.findByIdWithChildren(departmentId);
        if (department == null) {
            throw new IllegalArgumentException("Департамент не найден: " + departmentId);
        }

        // Собираем все ID (текущий + все потомки)
        Set<Integer> allDeptIds = new HashSet<>();
        collectDepartmentIds(department, allDeptIds);

        // Получаем все связи для этих департаментов
        List<DepartmentRole> allRoles = new ArrayList<>();
        for (Integer deptId : allDeptIds) {
            allRoles.addAll(departmentRoleRepository.findByDepartmentId(deptId));
        }

        // Возвращаем уникальные ID ролей
        return allRoles.stream()
                .map(dr -> dr.getRole().getId())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Рекурсивно собирает все ID департаментов (текущий + потомки).
     */
    private void collectDepartmentIds(Department department, Set<Integer> result) {
        result.add(department.getId());
        if (department.getChildren() != null) {
            for (Department child : department.getChildren()) {
                collectDepartmentIds(child, result);
            }
        }
    }
}