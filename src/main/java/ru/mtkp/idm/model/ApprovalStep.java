package ru.mtkp.idm.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Шаг согласования заявки (таблица approval_step).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "approval_step", indexes = {
        @Index(name = "idx_approval_step_request", columnList = "request_id")
})
public class ApprovalStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @Column(name = "step_name", nullable = false, length = 64)
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private StepName stepName = StepName.LINE_MANAGER;

    @Column(name = "decision", nullable = false, length = 32)
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private AppDecision decision = AppDecision.PENDING;

    @Column(name = "notes")
    private String notes;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;
}


