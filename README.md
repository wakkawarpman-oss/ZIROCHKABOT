# ZIROCHKABOT 🌟

> Автономний бот для прийому та управління бронюванням столиків у ресторані.  
> Працює 24/7 на старому Android-планшеті (Samsung Galaxy Tab з One UI).

---

## Зміст

- [Як це працює](#як-це-працює)
- [Структура проєкту](#структура-проєкту)
- [Швидкий старт](#швидкий-старт)
  - [1. Підготовка Google Sheets](#1-підготовка-google-sheets)
  - [2. Telegram-бот](#2-telegram-бот)
  - [3. Деплой Google Apps Script](#3-деплой-google-apps-script)
  - [4. Налаштування тригерів](#4-налаштування-тригерів)
  - [5. Перевірка роботи](#5-перевірка-роботи)
- [HTTP API](#http-api)
- [Запуск тестів](#запуск-тестів)
- [Спеціальні налаштування для Samsung (One UI)](#спеціальні-налаштування-для-samsung-one-ui)
- [Усунення несправностей](#усунення-несправностей)
- [Ліцензія](#ліцензія)

---

## Як це працює

```
Клієнт/Telegram-бот
        │  POST JSON
        ▼
Google Apps Script (Web App URL)
        │
        ├── Зберігає бронювання → Google Sheets
        │
        ├── Надсилає сповіщення власнику/персоналу → Telegram
        │
        └── Кожні 10 хв перевіряє "час → 1 год до броні"
                │
                └── Надсилає нагадування клієнту → Telegram
```

Android-планшет (Samsung Galaxy Tab) слугує постійно увімкненою нодою:
- тримає відкритим Telegram для прийому повідомлень від власника/персоналу;
- (опціонально) через **MacroDroid** форвардить події з Instagram у Google Apps Script.

---

## Структура проєкту

```
google-apps-script/
├── appsscript.json          — маніфест GAS (таймзона, права)
├── Config.gs                — конфігурація з Script Properties
├── Booking.gs               — модель бронювання, валідація, бізнес-логіка
├── SheetManager.gs          — CRUD-операції з Google Sheets
├── TelegramBot.gs           — Telegram Bot API, шаблони повідомлень
├── Reminders.gs             — планування та відправка нагадувань
├── Code.gs                  — HTTP-точки входу (doGet / doPost) та тригери
└── tests/
    ├── BookingTest.gs       — юніт-тести моделі бронювання
    └── ReminderTest.gs      — юніт-тести логіки нагадувань
```

---

## Швидкий старт

### 1. Підготовка Google Sheets

1. Відкрийте [Google Sheets](https://sheets.google.com) і створіть нову таблицю.
2. Запишіть **ID таблиці** з рядка адреси:  
   `https://docs.google.com/spreadsheets/d/`**`<SPREADSHEET_ID>`**`/edit`
3. Лист «Bookings» буде створено автоматично при першому зверненні.

---

### 2. Telegram-бот

1. У Telegram напишіть `@BotFather` → `/newbot` → дайте ім'я та username.
2. Збережіть отриманий **токен** (вигляд: `123456:ABC-DEF...`).
3. Надішліть будь-яке повідомлення своєму боту, потім відкрийте:  
   `https://api.telegram.org/bot<TOKEN>/getUpdates`  
   Знайдіть `"chat":{"id": <ВАШ_CHAT_ID>}` — це ваш **OWNER_CHAT_ID**.

---

### 3. Деплой Google Apps Script

1. Перейдіть на [script.google.com](https://script.google.com) → **Новий проєкт**.
2. Скопіюйте файли з папки `google-apps-script/` до редактора  
   *(кожен `.gs`-файл → окрема вкладка, `appsscript.json` — через «Показати файл manifest»)*.
3. **File → Project properties → Script properties** — додайте властивості:

   | Ключ | Значення |
   |---|---|
   | `SPREADSHEET_ID` | ID таблиці з кроку 1 |
   | `SHEET_NAME` | `Bookings` (або інше ім'я) |
   | `TELEGRAM_BOT_TOKEN` | токен з кроку 2 |
   | `OWNER_CHAT_ID` | ваш Telegram chat ID |
   | `STAFF_CHAT_IDS` | chat ID персоналу через кому (або порожньо) |
   | `REMINDER_MINUTES_BEFORE` | `60` (хвилин до броні для нагадування) |
   | `TIMEZONE` | `Europe/Kyiv` |

4. **Deploy → New deployment**:
   - Type: **Web app**
   - Execute as: **Me**
   - Who has access: **Anyone** *(потрібно для прийому webhook від Telegram/зовнішніх систем)*
5. Скопіюйте отриманий **Web App URL** — це ваш endpoint.

---

### 4. Налаштування тригерів

Відкрийте файл `Code.gs` та натисніть **Run → `setupTriggers`**.  
Це встановить автоматичний запуск `runReminderJob` кожні 10 хвилин.

> ⚠️ Apps Script попросить дозволи на Google Sheets та зовнішні HTTP-запити — **Allow**.

---

### 5. Перевірка роботи

```bash
# Ping (перевірка що бот живий)
curl "https://script.google.com/macros/s/<DEPLOYMENT_ID>/exec?action=ping"
# Очікувана відповідь: {"ok":true,"message":"ZIROCHKABOT is running"}

# Нове бронювання
curl -X POST "https://script.google.com/macros/s/<DEPLOYMENT_ID>/exec" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "createBooking",
    "clientName": "Оля Коваль",
    "clientPhone": "+380501234567",
    "clientTelegramId": "123456789",
    "guestCount": 3,
    "bookingTime": "2025-12-31T19:00:00+02:00",
    "tableNumber": "5",
    "notes": "алергія на горіхи"
  }'
```

---

## HTTP API

Базовий URL: `https://script.google.com/macros/s/<DEPLOYMENT_ID>/exec`

### GET `?action=ping`

Health check.

**Відповідь:**
```json
{ "ok": true, "message": "ZIROCHKABOT is running" }
```

---

### POST — Нове бронювання

```json
{
  "action": "createBooking",
  "clientName": "string (обов'язково)",
  "clientPhone": "string (або clientTelegramId)",
  "clientTelegramId": "string (або clientPhone)",
  "guestCount": 2,
  "bookingTime": "ISO-8601 datetime (обов'язково)",
  "tableNumber": "string (опційно)",
  "notes": "string (опційно)"
}
```

**Відповідь:**
```json
{ "ok": true, "id": "uuid-бронювання" }
```

---

### POST — Зміна статусу

```json
{
  "action": "updateStatus",
  "id": "uuid-бронювання",
  "status": "pending | confirmed | cancelled | completed"
}
```

**Відповідь:**
```json
{ "ok": true }
```

---

### POST — Список бронювань

```json
{ "action": "getBookings" }
```

**Відповідь:**
```json
{
  "ok": true,
  "bookings": [{ "id": "...", "clientName": "...", ... }]
}
```

---

## Запуск тестів

Тести написані на чистому GAS JavaScript (без зовнішніх бібліотек) і не звертаються до Google Sheets або Telegram.

1. Відкрийте Apps Script Editor.
2. Перейдіть на вкладку `tests/ReminderTest.gs`.
3. Виберіть функцію `runAllTests` і натисніть ▶ **Run**.
4. Перегляньте результати: **View → Logs**.

Успішний запуск виглядає так:
```
=== BookingTest suite ===
✅ PASS  createBooking — sets defaults correctly
✅ PASS  createBooking — guestCount defaults to 1 when missing
...
=== ReminderTest suite ===
✅ PASS  minutesUntilBooking — returns positive value for future booking
✅ PASS  shouldSendReminder — true when confirmed, not sent, within window
...
```

---

## Спеціальні налаштування для Samsung (One UI)

> Цей розділ призначений для налаштування Samsung Galaxy Tab як постійно увімкненого сервера.  
> Кроки виконуються один раз після встановлення потрібних програм.

### 🔋 Вимкнення оптимізації батареї

Без цього налаштування Samsung автоматично зупинятиме Telegram та інші програми у фоні.

1. **Налаштування** → **Акумулятор і догляд за пристроєм** → **Акумулятор**.
2. Натисніть **Фонове використання додатків** (або **Ліміт використання у фоні**).
3. Знайдіть **Telegram** (і **MacroDroid**, якщо використовуєте) у списку.
4. Для кожного додатка виберіть **Не оптимізувати**.

> ✅ Результат: програми залишатимуться активними, навіть якщо ви не користуєтеся планшетом.

---

### 📌 Закріплення додатка у пам'яті

1. Натисніть кнопку **«Останні програми»** (квадрат або три риски внизу).
2. Знайдіть карточку **Telegram** (або MacroDroid).
3. Зробіть **довгий дотик** на іконці програми у карточці → виберіть **Заблокувати** або **Тримати відкритим**.

> ✅ Результат: програму не буде закрито під час очищення пам'яті.

---

### 🖥️ Екран не вимикається під час заряджання

1. **Налаштування** → **Про планшет** → натисніть **7 разів** на **Номер збірки** — активується режим розробника.
2. Поверніться в **Налаштування** → **Параметри розробника**.
3. Увімкніть **Не вимикати екран під час заряджання**.

> ✅ Результат: планшет на зарядці завжди матиме активний екран — зручно для моніторингу.

---

### 🌙 Режим «Не турбувати»

Щоб не відволікатися на системні сповіщення під час роботи:

1. **Налаштування** → **Звуки та вібрація** → **Не турбувати**.
2. Увімкніть та налаштуйте **Винятки** — дозвольте сповіщення лише від **Telegram**.

---

### ⚡ Один раз — на старті

| Крок | Де | Дія |
|---|---|---|
| 1 | Налаштування батареї | Вимкнути оптимізацію для Telegram |
| 2 | Меню останніх програм | Закріпити Telegram у пам'яті |
| 3 | Параметри розробника | Увімкнути «Не вимикати під час заряджання» |
| 4 | Підключити зарядку | Залишити планшет постійно на зарядці |
| 5 | Telegram | Переконатися, що бот активний та отримує повідомлення |

---

## Усунення несправностей

**Бот не відповідає на повідомлення**
- Перевірте, що Web App задеплоєно і URL правильний.
- Переконайтесь, що `TELEGRAM_BOT_TOKEN` та `OWNER_CHAT_ID` у Script Properties збігаються з реальними.

**Нагадування не надсилаються**
- Перевірте, що тригер встановлено: Apps Script Editor → **Triggers** (іконка годинника) → має бути `runReminderJob` кожні 10 хв.
- Статус бронювання має бути `confirmed` (не `pending`).

**Samsung зупиняє Telegram**
- Виконайте всі кроки розділу [Спеціальні налаштування для Samsung (One UI)](#спеціальні-налаштування-для-samsung-one-ui).
- Перевірте розділ **Налаштування → Акумулятор → Додатки у фоні** — Telegram не повинен бути обмежений.

**Помилка «SPREADSHEET_ID not found»**
- Переконайтесь, що скрипт має доступ до таблиці (той самий Google-акаунт або доступ надано).

---

## Ліцензія

MIT — дивіться [LICENSE](LICENSE).
