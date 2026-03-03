# ZIROCHKABOT

Набір файлів для прийому повідомлень з Instagram у Google Sheets за допомогою Google Apps Script і шаблону MacroDroid.

## Файли в репозиторії
- `Code.gs` – скрипт Google Apps Script, який приймає POST-запити й додає дані в Google Sheet.
- `instagram_to_sheets.mdr` – шаблон MacroDroid, який потрібно додати до релізу (наприклад, `v1.0.0`).

## Кроки налаштування
1. **Google Sheet**: створіть порожню таблицю в Google Drive.
2. **Apps Script**:
   - Відкрийте таблицю → Extensions → Apps Script.
   - Замініть вміст редактора на код із `Code.gs`. За потреби змініть назву аркуша `SHEET_NAME`.
   - Deploy → New deployment → Web app: *Execute as*: Me, *Who has access*: Anyone. Скопіюйте URL Web App.
3. **MacroDroid**:
   - Завантажте шаблон з релізу: [instagram_to_sheets.mdr](https://github.com/wakkawarpman-oss/ZIROCHKABOT/releases/download/v1.0.0/instagram_to_sheets.mdr).
   - Імпортуйте файл у MacroDroid та у дії HTTP Request вставте URL вашого Web App.
   - Тіло запиту (JSON), яке надсилає MacroDroid:
     ```json
     {
       "username": "<instagram @>",
       "fullName": "<Ім'я>",
       "message": "<Текст повідомлення>",
       "profileUrl": "https://instagram.com/<username>",
       "source": "instagram"
     }
     ```
4. **Перевірка**: надішліть тестове повідомлення й переконайтеся, що рядок з'явився в таблиці.

## Ліцензія
Проєкт ліцензовано на умовах MIT (див. файл `LICENSE`).
