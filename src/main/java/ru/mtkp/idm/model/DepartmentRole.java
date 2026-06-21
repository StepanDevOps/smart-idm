package ru.mtkp.idm.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "department_role", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"department_id", "role_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}