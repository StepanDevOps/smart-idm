package ru.mtkp.idm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.mtkp.idm.model.Department;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.children WHERE d.id = :id")
    Department findByIdWithChildren(@Param("id") Integer id);

    @Query("SELECT d FROM Department d WHERE d.parentUnit IS NULL")
    List<Department> findRootDepartments();

    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.users WHERE d.id = :id")
    Department findByIdWithUsers(@Param("id") Integer id);
}