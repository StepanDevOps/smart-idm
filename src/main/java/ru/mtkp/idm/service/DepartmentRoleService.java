package ru.mtkp.idm.service;

import ru.mtkp.idm.model.DepartmentRole;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления связями Department ↔ Role.
 */
public interface DepartmentRoleService {

    /**
     * Получить все связи.
     *
     * @return список всех связей
     */
    List<DepartmentRole> findAll();

    /**
     * Создать связь департамента с ролью.
     *
     * @param departmentId идентификатор департамента
     * @param roleId идентификатор роли
     * @return созданная связь
     */
    DepartmentRole createAssignment(Integer departmentId, Integer roleId);

    /**
     * Удалить связь департамента с ролью.
     *
     * @param id идентификатор связи
     * @return true если удалено
     */
    boolean deleteAssignment(Integer id);

    /**
     * Удалить все связи для департамента.
     *
     * @param departmentId идентификатор департамента
     */
    void deleteByDepartmentId(Integer departmentId);

    /**
     * Получить все связи для департамента.
     *
     * @param departmentId идентификатор департамента
     * @return список связей
     */
    List<DepartmentRole> findByDepartmentId(Integer departmentId);

    /**
     * Получить все связи для роли.
     *
     * @param roleId идентификатор роли
     * @return список связей
     */
    List<DepartmentRole> findByRoleId(Integer roleId);

    /**
     * Получить связь по ID.
     *
     * @param id идентификатор связи
     * @return Optional со связью
     */
    Optional<DepartmentRole> findById(Integer id);

    /**
     * Проверить, есть ли связь между департаментом и ролью.
     *
     * @param departmentId идентификатор департамента
     * @param roleId идентификатор роли
     * @return true если связь существует
     */
    boolean existsByDepartmentIdAndRoleId(Integer departmentId, Integer roleId);

    /**
     * Получить все роли, назначенные на департамент (с подчинёнными).
     *
     * @param departmentId идентификатор департамента
     * @return список уникальных ролей
     */
    List<Integer> getAllRolesForDepartmentWithChildren(Integer departmentId);
}