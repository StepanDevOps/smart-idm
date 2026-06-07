package ru.mtkp.idm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Целевая система (таблица target_systems).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "target_systems", indexes = {
        @Index(name = "idx_target_system_name", columnList = "name")
})
public class TargetSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "type", nullable = false, length = 32)
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private SystemType type;

    @Column(name = "connection_details")
    private String connectionDetails;

    @Column(name = "sync_enabled", nullable = false)
    private Boolean syncEnabled = Boolean.TRUE;
}


