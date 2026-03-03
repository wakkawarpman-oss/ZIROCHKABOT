# Макрос MacroDroid для Instagram-бота

> Шаблонний URL в HTTP дії має бути порожнім. Вставте свій URL Apps Script після деплою.

## 1) Обов'язкові змінні

- `restaurant_name = "Зірочка"`
- `restaurant_instagram = "@zirochka.kyiv"`
- `greeting_morning = "Доброго ранку!"`
- `greeting_afternoon = "Доброго дня!"`
- `greeting_evening = "Доброго вечора!"`
- `greeting_night = "Доброї ночі!"`
- `reply_formal = "Дякуємо за звернення. Підтвердимо бронювання найближчим часом."`
- `reply_friendly = "Щиро дякуємо за повідомлення! Уже обробляємо ваше бронювання."`
- `reply_apologetic = "Перепрошуємо за затримку. Будь ласка, вибачте."`
- `reply_neutral = "Дякуємо за повідомлення, працюємо над вашим запитом."`
- `apology_text = "Перепрошуємо за затримку"`
- `enjoy_phrase = "Сподіваємося, вам смакуватиме!"`

## 2) Trigger

- `Trigger -> Notification`
- App: `Instagram`
- Filter: вхідні DM або текст містить `@zirochka.kyiv`

## 3) Логіка макросу

1. **Визначення привітання за часом**
   - 06:00–11:59 -> `greeting_morning`
   - 12:00–16:59 -> `greeting_afternoon`
   - 17:00–22:59 -> `greeting_evening`
   - 23:00–05:59 -> `greeting_night`
2. **Визначення тону відповіді**
   - якщо текст містить `довго`, `чекаю`, `затримка` -> `reply_apologetic`
   - якщо містить `дякую`, `клас`, `смачно` -> `reply_friendly`
   - якщо містить `брон`, `столик`, `замов` -> `reply_formal`
   - інакше -> `reply_neutral`
3. **Формування тексту**
   - `{greeting} Ви написали до ресторану {restaurant_name} ({restaurant_instagram}). {tone} {enjoy_phrase}`
4. **HTTP POST у Apps Script**
   - Method: `POST`
   - URL: ``
   - Header: `Content-Type: application/json`
   - Body:
     ```json
     {
       "username": "{notification_title}",
       "message": "{notification_text}"
     }
     ```
5. **Відправлення в Instagram**
   - `Launch App -> Instagram`
   - `UI Interaction -> Paste`
   - `UI Interaction -> Click "Надіслати"`

## 4) Візуальна інструкція (що сфотографувати)

Якщо `.mdr` не генерується автоматично, додайте ці скріншоти для персоналу:

- Скрін #1: налаштування Trigger (Notification -> Instagram)
- Скрін #2: список усіх змінних і значень
- Скрін #3: блок If/Else для тону відповіді
- Скрін #4: HTTP Request з порожнім URL
- Скрін #5: фінальний список дій і кнопка Enable

## 5) Експорт у `.mdr`

- `MacroDroid -> Menu -> Export/Import -> Export Macro -> Share as .mdr`
