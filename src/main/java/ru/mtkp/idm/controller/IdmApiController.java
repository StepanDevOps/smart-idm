package ru.mtkp.idm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.mtkp.idm.service.WorkflowEngineService;

import java.util.Map;

/**
 * REST API для внешних систем (HR-интеграция).
 * Точка входа для событий от HR-системы (1C, SAP, BambooHR и т.д.).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class IdmApiController {

    private final WorkflowEngineService workflowEngineService;

    /**
     * Обработка HR-события (Joiner/Mover/Leaver).
     * Вызывается из HR-системы при приёме/переводe/увольнении сотрудника.
     *
     * @param event событие (HR_EVENT_JOINER, HR_EVENT_MOVER, HR_EVENT_LEAVER)
     * @param userId ID сотрудника
     * @param details дополнительные данные (подразделение, должность и т.д.)
     * @return результат обработки
     */
    @PostMapping("/events/hr")
    public ResponseEntity<Map<String, Object>> handleHrEvent(
            @RequestParam String event,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "") String details) {

        long startTime = System.currentTimeMillis();
        log.info("API: HR-событие получено: event={}, userId={}, details={}", event, userId, details);

        try {
            boolean result = workflowEngineService.handleIdmEvent(event, userId, details);

            long duration = System.currentTimeMillis() - startTime;
            log.info("API: HR-событие обработано за {} мс", duration);

            return ResponseEntity.ok(Map.of(
                    "status", result ? "SUCCESS" : "FAILED",
                    "event", event,
                    "userId", userId,
                    "durationMs", duration
            ));

        } catch (IllegalArgumentException e) {
            log.warn("API: Ошибка валидации: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("API: Неожиданная ошибка при обработке события", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "ERROR",
                    "message", "Внутренняя ошибка сервера"
            ));
        }
    }

    /**
     * Проверка работоспособности API (health check).
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "Smart IDM API"));
    }
}
