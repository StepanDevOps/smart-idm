package ru.mtkp.idm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mtkp.idm.model.Department;
import ru.mtkp.idm.model.DepartmentRole;
import ru.mtkp.idm.model.Role;
import ru.mtkp.idm.repository.DepartmentRepository;
import ru.mtkp.idm.repository.DepartmentRoleRepository;
import ru.mtkp.idm.repository.RoleRepository;
import ru.mtkp.idm.service.impl.DepartmentRoleServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для DepartmentRoleServiceImpl.
 * Проверяют INDIRECT назначения ролей через департаменты.
 */
@ExtendWith(MockitoExtension.class)
class DepartmentRoleServiceImplTest {

    @Mock
    private DepartmentRoleRepository departmentRoleRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private DepartmentRoleServiceImpl departmentRoleService;

    private Department parentDept;
    private Department childDept;
    private DepartmentRole parentRole;
    private DepartmentRole childRole;

    @BeforeEach
    void setUp() {
        parentDept = new Department();
        parentDept.setId(1);
        parentDept.setName("Родительский отдел");
        parentDept.setCode("PARENT-001");

        childDept = new Department();
        childDept.setId(2);
        childDept.setName("Дочерний отдел");
        childDept.setCode("CHILD-001");
        childDept.setParentUnit(parentDept);

        // Добавляем childDept в children parentDept для корректной работы getAllRolesForDepartmentWithChildren
        List<Department> children = new ArrayList<>();
        children.add(childDept);
        parentDept.setChildren(children);

        Role parentRoleObj = new Role();
        parentRoleObj.setId(100);
        parentRoleObj.setName("Роль родителя");

        Role childRoleObj = new Role();
        childRoleObj.setId(200);
        childRoleObj.setName("Роль ребёнка");

        parentRole = new DepartmentRole();
        parentRole.setId(1);
        parentRole.setDepartment(parentDept);
        parentRole.setRole(parentRoleObj);

        childRole = new DepartmentRole();
        childRole.setId(2);
        childRole.setDepartment(childDept);
        childRole.setRole(childRoleObj);
    }

    @Test
    void testGetAllRolesForDepartmentWithChildren_IncludesParentAndChildRoles() {
        // Arrange
        when(departmentRepository.findByIdWithChildren(1)).thenReturn(parentDept);
        when(departmentRoleRepository.findByDepartmentId(1)).thenReturn(List.of(parentRole));
        when(departmentRoleRepository.findByDepartmentId(2)).thenReturn(List.of(childRole));

        // Act
        List<Integer> roleIds = departmentRoleService.getAllRolesForDepartmentWithChildren(1);

        // Assert
        assertEquals(2, roleIds.size());
        assertTrue(roleIds.contains(100)); // Роль родителя
        assertTrue(roleIds.contains(200)); // Роль ребёнка
    }

    @Test
    void testGetAllRolesForDepartmentWithChildren_NoChildren_ReturnsOnlyParentRoles() {
        // Arrange
        when(departmentRepository.findByIdWithChildren(1)).thenReturn(parentDept);
        when(departmentRoleRepository.findByDepartmentId(1)).thenReturn(List.of(parentRole));

        // Act
        List<Integer> roleIds = departmentRoleService.getAllRolesForDepartmentWithChildren(1);

        // Assert
        assertEquals(1, roleIds.size());
        assertEquals(100, roleIds.get(0));
    }

    @Test
    void testGetAllRolesForDepartmentWithChildren_NoRoles_ReturnsEmpty() {
        // Arrange
        when(departmentRepository.findByIdWithChildren(1)).thenReturn(parentDept);
        when(departmentRoleRepository.findByDepartmentId(1)).thenReturn(List.of());

        // Act
        List<Integer> roleIds = departmentRoleService.getAllRolesForDepartmentWithChildren(1);

        // Assert
        assertTrue(roleIds.isEmpty());
    }

    @Test
    void testCreateAssignment_SavesDepartmentRole() {
        // Arrange
        Role mockRole = new Role();
        mockRole.setId(100);
        when(departmentRepository.findById(1)).thenReturn(Optional.of(parentDept));
        when(roleRepository.findById(100)).thenReturn(Optional.of(mockRole));
        when(departmentRoleRepository.save(any(DepartmentRole.class))).thenReturn(parentRole);

        // Act
        departmentRoleService.createAssignment(1, 100);

        // Assert
        verify(departmentRoleRepository, times(1)).save(any(DepartmentRole.class));
    }

    @Test
    void testDeleteAssignment_ById() {
        // Arrange
        when(departmentRoleRepository.existsById(1)).thenReturn(true);

        // Act
        boolean deleted = departmentRoleService.deleteAssignment(1);

        // Assert
        assertTrue(deleted);
        verify(departmentRoleRepository, times(1)).deleteById(1);
    }
}
