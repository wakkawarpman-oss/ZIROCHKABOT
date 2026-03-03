# Інтеграція Apps Script

`Code.gs` — вебхук для збереження запитів на бронювання в Google Sheets.

## Налаштування

1. Відкрийте `https://sheets.new`.
2. Створіть лист `Бронювання`.
3. Відкрийте `Extensions -> Apps Script`.
4. Замініть дефолтний код вмістом `Code.gs`.
5. `Deploy -> New deployment -> Web app`.
6. Параметри:
   - `Execute as`: `Me`
   - `Who has access`: `Anyone`
7. Скопіюйте URL вебдодатка (`.../exec`) і підставте в MacroDroid.

## Формат вхідних даних (POST)

Мінімум:

```json
{
  "username": "guest_account",
  "message": "Хочу столик на 19:00"
}
```

Розширений варіант (для POS/бота):

```json
{
  "source": "instagram_dm",
  "instagram_username": "guest_account",
  "guest_name": "Anna",
  "phone": "+380501112233",
  "guests": "4",
  "visit_date": "2026-03-05",
  "visit_time": "19:00",
  "message": "Столик на вечір, будь ласка",
  "orderSummary": "",
  "total": ""
}
```

## Перевірка

- GET на URL повертає health-check JSON.
- POST додає рядок у лист `Бронювання`.
- Помилки пишуться в `Executions` / `Logs` Apps Script.

## Нотатки

- Для фіксованої таблиці заповніть `CONFIG.spreadsheetId`.
- Не зберігайте секрети/токени в репозиторії.
