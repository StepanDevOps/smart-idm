#!/bin/bash

# ============================================================
# Нагрузочное тестирование Реконсиляции (Smart IDM)
# Эндпоинт: POST /api/v1/reconciliation/hr-feed
# ============================================================

API_URL="http://localhost:8080/api/v1/reconciliation/hr-feed"
DELAY=1  # Задержка между запросами в секундах

# Переменные для статистики
SUCCESS=0
FAILED=0
TOTAL_TIME=0
MIN_TIME=999999
MAX_TIME=0
COUNTER=0

# Цвета
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}  Smart IDM Reconciliation Load Test${NC}"
echo -e "${YELLOW}========================================${NC}"
echo -e "URL: ${CYAN}${API_URL}${NC}"
echo -e "Method: POST (JSON Payload)"
echo -e "Delay: ${CYAN}${DELAY}s${NC}"
echo -e "Готов! Жму Ctrl+C для остановки..."
echo -e "${YELLOW}----------------------------------------${NC}"

# Функция вывода результатов
cleanup() {
    echo -e "\n\n${YELLOW}========================================${NC}"
    echo -e "${YELLOW}  РЕЗУЛЬТАТЫ ТЕСТА${NC}"
    echo -e "${YELLOW}========================================${NC}"

    TOTAL=$((SUCCESS + FAILED))

    echo -e "Всего запросов:   $TOTAL"
    echo -e "Успешных:         ${GREEN}${SUCCESS}${NC}"
    echo -e "Неудачных:        ${RED}${FAILED}${NC}"

    if [ $SUCCESS -gt 0 ]; then
        AVG=$((TOTAL_TIME / SUCCESS))
        echo -e "Среднее время:    ${CYAN}${AVG}ms${NC}"
        echo -e "Мин. время:       ${CYAN}${MIN_TIME}ms${NC}"
        echo -e "Макс. время:      ${CYAN}${MAX_TIME}ms${NC}"
    fi

    ERROR_RATE=0
    if [ $TOTAL -gt 0 ]; then
        ERROR_RATE=$((FAILED * 100 / TOTAL))
    fi
    echo -e "Ошибка:           ${RED}${ERROR_RATE}%${NC}"

    # Сохранить в CSV
    RESULTS_FILE="recon_loadtest_results_$(date +%Y%m%d_%H%M%S).csv"
    cat > "$RESULTS_FILE" << EOF
timestamp,$(date +%Y-%m-%d_%H:%M:%S)
total_requests,$TOTAL
success,$SUCCESS
failed,$FAILED
avg_ms,$AVG
min_ms,$MIN_TIME
max_ms,$MAX_TIME
error_rate,$ERROR_RATE%
EOF

    echo -e "\nРезультаты сохранены в: ${CYAN}${RESULTS_FILE}${NC}"
    echo -e "${YELLOW}========================================${NC}"
    exit 0
}

# Перехват Ctrl+C
trap cleanup SIGINT

# Цикл тестирования
while true; do
    COUNTER=$((COUNTER + 1))
    USER_ID=$(( (COUNTER % 10) + 1 )) # Используем ID 1..10, чтобы попадать в БД

    START_TIME=$(date +%s%N)

    # Отправка JSON-запроса
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
        -X POST \
        -H "Content-Type: application/json" \
        -d "{\"userId\": $USER_ID, \"login\": \"testuser_$USER_ID\", \"department\": \"IT\"}" \
        "${API_URL}" 2>/dev/null)

    END_TIME=$(date +%s%N)
    ELAPSED=$(( (END_TIME - START_TIME) / 1000000 )) # в мс

    if [ "$HTTP_CODE" = "200" ]; then
        SUCCESS=$((SUCCESS + 1))

        # Статистика времени
        if [ $ELAPSED -lt $MIN_TIME ]; then MIN_TIME=$ELAPSED; fi
        if [ $ELAPSED -gt $MAX_TIME ]; then MAX_TIME=$ELAPSED; fi
        TOTAL_TIME=$((TOTAL_TIME + ELAPSED))

        if [ $((COUNTER % 5)) -eq 0 ]; then
             CURRENT_AVG=$((TOTAL_TIME / SUCCESS))
             echo -e "[$COUNTER] OK | ${CURRENT_AVG}ms avg | Status: ${HTTP_CODE} ${GREEN}✓${NC}"
        fi
    else
        FAILED=$((FAILED + 1))
        echo -e "[$COUNTER] FAIL | ${ELAPSED}ms | Status: ${HTTP_CODE} ${RED}✗${NC}"
    fi

    sleep $DELAY
done