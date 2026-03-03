# Проєкт «Зірочка Бот»

Два в одному: **Instagram-бот для бронювань** + **POS-додаток Zirochka POS** для прийому замовлень у ресторані «Зірочка». Усі дані синхронізуються з **Google Sheets** та, за потреби, дублюються у **Telegram**. Працює офлайн, з подальшою синхронізацією.

---

## Швидке встановлення (≈5 хвилин)
⚠️ Посилання активуються після публікації першого релізу; до того використовуйте інструкцію зі збірки нижче.
1. **Завантажте APK**: [ZirochkaPOS.apk](https://github.com/wakkawarpman-oss/ZIROCHKABOT/releases/download/v1.0.0/ZirochkaPOS.apk)  
   _QR-код:_ (згенеруйте з цього посилання або додайте готове зображення).
2. **Імпортуйте макрос**: `zirochka_instagram_bot.mdr` з релізу `v1.0.0`.  
   _QR-код:_ посилання на той самий файл.
3. **Інструкція + меню**:  
   - [menu_zirochka.json](app/src/main/assets/menu_zirochka.json)  
   - [STAFF_GUIDE (PDF/MD)](docs/STAFF_GUIDE.md)  
   - README.md (цей файл).

_Примітка: посилання на файли активуються після публікації релізу v1.0.0; до того можна зібрати APK та .mdr вручну за інструкцією нижче._

> Якщо немає готового релізу — зберіть APK через Android Studio, згенеруйте `.mdr` у MacroDroid (Export Macro), і прикріпіть до релізу `v1.0.0`.

---

## Детальна інструкція для Instagram-бота
### 1. Google Sheets + Apps Script
1. Створіть таблицю **"Бронювання"** у Google Sheets (перший рядок: Дата | Instagram | Повідомлення).
2. Відкрийте **Extensions → Apps Script** → вставте код з [`macros/Code.gs`](macros/Code.gs).
3. **Deploy → New deployment → Web app**:  
   - Execute as: *Me*  
   - Who has access: *Anyone*  
   - Натисніть Deploy та скопіюйте URL (залиште в MacroDroid порожнім, додайте після деплою).
4. Перевірка: відкрийте URL у браузері — повинно повернути `{"status":"ok" ...}`.

### 2. MacroDroid
1. Встановіть MacroDroid на планшет.  
2. Імпорт `.mdr` або створіть за інструкцією [`macros/MACRODROID.md`](macros/MACRODROID.md).  
3. Заповніть змінні:
   - `restaurant_name = "Зірочка"`  
   - `restaurant_instagram = "@zirochka.kyiv"`  
   - Привітання: «Доброго ранку/дня/вечора/ночі»  
   - Вибачення: «Перепрошуємо за затримку», «Будь ласка, вибачте»  
   - Побажання: «Пригощайтеся, будь ласка!», «Сподіваємося, вам смакуватиме!»
4. Вставте URL Apps Script (після деплою).

### 3. Налаштування Samsung
- Вимкніть оптимізацію батареї для MacroDroid та Zirochka POS.  
- Увімкніть **App pinning** (Закріплення застосунку).  
- Дайте MacroDroid доступ до сповіщень та накладання поверх інших вікон.

---

## Детальна інструкція для POS-додатка
1. **Встановлення APK**: скачайте з релізу або зберіть у Android Studio (`./gradlew assembleDebug`).  
2. **Налаштування ключів** (опційно, для Telegram/Sheets):  
   - У `local.properties` або `gradle.properties` задайте:  
     ```
     telegram.bot.token=ВАШ_БОТ_ТОКЕН
     telegram.chat.id=ВАШ_CHAT_ID
     sheets.script.url=URL_вашого_Apps_Script
     ```
   - BuildConfig підхоплює ці значення автоматично.
3. **Перший запуск**: застосунок прочитає меню з `assets/menu_zirochka.json` і збереже у Room.  
4. **Офлайн-робота**: замовлення пишуться в Room і синхронізуються в Sheets/Telegram при появі мережі.  
5. **Оновлення меню**: вкладка **Адмін** → «Перечитати меню з assets».

---

## Інструкція для персоналу
- Короткий довідник: [`docs/STAFF_GUIDE.md`](docs/STAFF_GUIDE.md).  
- Версія для друку: експортуйте у PDF (можна прямо з Markdown через браузер або pandoc).

---

## Вирішення проблем
- **Apps Script не відповідає**: перевірте деплой як Web App і права доступу, URL у MacroDroid.  
- **Telegram не надсилає**: переконайтесь у правильності токена та chat_id у `gradle.properties`.  
- **Меню не оновилось**: вкладка Адмін → «Перечитати меню з assets».  
- **Немає інтернету**: замовлення зберігаються локально, синхронізуються пізніше.

---

## Ліцензія
[MIT](LICENSE)
