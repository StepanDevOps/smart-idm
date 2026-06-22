package ru.mtkp.idm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.mtkp.idm.controller.dto.HrFeedPayload;
import ru.mtkp.idm.model.User;
import ru.mtkp.idm.repository.UserRepository;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/reconciliation")
@RequiredArgsConstructor
public class ReconciliationController {

    private final UserRepository userRepository;

    /**
     * Эндпоинт для симуляции реконсиляции.
     * HR-система шлёт данные о сотруднике, IDM проверяет их наличие.
     */
    @PostMapping("/hr-feed")
    public ResponseEntity<Map<String, Object>> processHrFeed(@RequestBody HrFeedPayload payload) {
        long startTime = System.currentTimeMillis();

        // Симуляция поиска пользователя в БД
        User user = userRepository.findById(payload.getUserId()).orElse(null);

        long duration = System.currentTimeMillis() - startTime;
        log.debug("Recon request processed for userId={} in {}ms", payload.getUserId(), duration);

        if (user != null) {
            return ResponseEntity.ok(Map.of(
                    "status", "MATCH",
                    "userId", payload.getUserId(),
                    "dbLogin", user.getLogin(),
                    "durationMs", duration
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "status", "MISSING",
                    "userId", payload.getUserId(),
                    "durationMs", duration
            ));
        }
    }
}