# Макрос MacroDroid для Instagram-бота

> URL Apps Script залиште порожнім — вставте власний після деплойменту.

## Змінні
- `restaurant_name` = `Зірочка`
- `restaurant_instagram` = `@zirochka.kyiv`
- `greeting_morning`, `greeting_afternoon`, `greeting_evening`, `greeting_night`
- `reply_formal`, `reply_friendly`, `reply_apologetic`, `reply_neutral`
- `apology_text`, `enjoy_phrase`

## Тригер
- **Notification** → App: Instagram → Text contains `@zirochka.kyiv` (або вхідні повідомлення Direct).

## Дії (покроково)
1. **Встановити змінні привітання за часом**  
- Якщо година 6–11 → `greeting = greeting_morning` (`Доброго ранку!`)  
- 12–16 → `greeting = greeting_afternoon` (`Доброго дня!`)  
- 17–22 → `greeting = greeting_evening` (`Доброго вечора!`)  
- Інакше → `greeting = greeting_night` (`Доброї ночі!`)
2. **Визначити тон відповіді за ключовими словами**  
   - Якщо текст містить `перепрошуємо`, `пізно`, `довго` → `tone = reply_apologetic` (`Будь ласка, вибачте...`)  
   - Якщо містить `дякую`, `клас`, `смачно` → `tone = reply_friendly` (`Щиро дякуємо...`)  
   - Інакше → `tone = reply_neutral`.
3. **Скласти відповідь**  
   ```
   {greeting}
   Ви написали до ресторану {restaurant_name} ({restaurant_instagram}).
   {tone}
   {enjoy_phrase} (наприклад: «Пригощайтеся, будь ласка!»)
   ```
4. **HTTP-запит**  
   - Method: `POST`  
   - URL: `{YOUR_APPS_SCRIPT_URL}` (залишити порожнім у шаблоні)  
   - Body (JSON): `{"username":"{notification_title}","message":"{notification_text}"}`  
   - Headers: `Content-Type: application/json`
5. **Відповідь у Instagram**  
   - Відкрити Instagram → Вхідні → Вставити з буфера → Надіслати.

## Підказка для швидкого створення макросу
1. Створіть макрос → Trigger: Notification → Instagram.  
2. Додайте блок *Variables* для 10+ змінних (вище).  
3. Додайте блок *Set Variable* для `greeting`.  
4. Додайте *If* для ключових слів та встановіть `tone`.  
5. Додайте *HTTP Request* з шаблоном тіла.  
6. Додайте *Launch App* → Instagram та *UI Interaction* → Paste → Send.

Для збереження у `.mdr`: *Menu → Export/Import → Export Macro → Share as .mdr*.
