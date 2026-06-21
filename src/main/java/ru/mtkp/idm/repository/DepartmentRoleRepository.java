package ru.mtkp.idm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mtkp.idm.model.DepartmentRole;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRoleRepository extends JpaRepository<DepartmentRole, Integer> {

    List<DepartmentRole> findByDepartmentId(Integer departmentId);

    Optional<DepartmentRole> findByDepartmentIdAndRoleId(Integer departmentId, Integer roleId);

    List<DepartmentRole> findByRoleId(Integer roleId);

    void deleteByDepartmentId(Integer departmentId);
}