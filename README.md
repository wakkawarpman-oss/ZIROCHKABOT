# Проєкт «Зірочка Бот»

Два в одному: **Instagram-бот для бронювань** + **POS-додаток Zirochka POS** для прийому замовлень у ресторані «Зірочка».  
Потік даних: Instagram/MacroDroid -> Apps Script -> Google Sheets, а POS працює офлайн через Room і синхронізує події при появі мережі.

---

## Швидке встановлення (≈5 хвилин)

1. Відкрийте [Releases](https://github.com/wakkawarpman-oss/ZIROCHKABOT/releases).
2. Завантажте:
   - [ZirochkaPOS.apk](https://github.com/wakkawarpman-oss/ZIROCHKABOT/releases/latest/download/ZirochkaPOS.apk)
   - [zirochka_instagram_bot.mdr](https://github.com/wakkawarpman-oss/ZIROCHKABOT/releases/latest/download/zirochka_instagram_bot.mdr)
   - [menu_zirochka.json](https://github.com/wakkawarpman-oss/ZIROCHKABOT/releases/latest/download/menu_zirochka.json)
3. Встановіть APK на Samsung-планшет.
4. Імпортуйте `.mdr` в MacroDroid і вставте свій URL Apps Script.
5. Запустіть `verify_setup.sh` для перевірки готовності.

QR для швидкого доступу до артефактів:

- APK: [QR](https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=https%3A%2F%2Fgithub.com%2Fwakkawarpman-oss%2FZIROCHKABOT%2Freleases%2Flatest%2Fdownload%2FZirochkaPOS.apk)
- MacroDroid: [QR](https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=https%3A%2F%2Fgithub.com%2Fwakkawarpman-oss%2FZIROCHKABOT%2Freleases%2Flatest%2Fdownload%2Fzirochka_instagram_bot.mdr)
- Інструкція: [QR](https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=https%3A%2F%2Fgithub.com%2Fwakkawarpman-oss%2FZIROCHKABOT)

Якщо релізу ще немає, використовуйте локальну збірку за інструкцією нижче.

---

## Детальна інструкція для Instagram-бота

### 1. Google Sheets + Apps Script

1. Створіть Google Sheet з назвою **«Бронювання»**.
2. Відкрийте `Extensions -> Apps Script`.
3. Вставте код з `apps-script/Code.gs` (або `macros/Code.gs`).
4. Опублікуйте Web App:
   - `Deploy -> New deployment -> Web app`
   - `Execute as: Me`
   - `Who has access: Anyone`
5. Скопіюйте URL типу `https://script.google.com/.../exec`.
6. Тест:
   - GET у браузері повертає JSON зі статусом `ok`
   - POST з `username` та `message` додає рядок у лист `Бронювання`

### 2. MacroDroid

1. Встановіть MacroDroid на планшет.
2. Імпортуйте `.mdr` або створіть вручну за `macros/MACRODROID.md`.
3. Встановіть змінні:
   - `restaurant_name = "Зірочка"`
   - `restaurant_instagram = "@zirochka.kyiv"`
   - привітання: `Доброго ранку/дня/вечора/ночі`
   - вибачення: `Перепрошуємо за затримку`, `Будь ласка, вибачте`
   - фрази: `Пригощайтеся, будь ласка!`, `Сподіваємося, вам смакуватиме!`
4. У HTTP-дії поле URL має бути порожнім у шаблоні, вставляється після вашого деплою Apps Script.

### 3. Налаштування Samsung

- Вимкніть оптимізацію батареї для `MacroDroid` і `Zirochka POS`.
- Закріпіть застосунки в recent apps.
- Увімкніть доступ MacroDroid до сповіщень, автозапуску й overlay.

---

## Детальна інструкція для POS-додатка

1. Встановіть `ZirochkaPOS.apk` (або зберіть через Android Studio).
2. За потреби заповніть ключі в `local.properties` або `gradle.properties`:

```properties
telegram.bot.token=YOUR_BOT_TOKEN
telegram.chat.id=YOUR_CHAT_ID
sheets.script.url=YOUR_APPS_SCRIPT_URL
```

3. Перший запуск імпортує `app/src/main/assets/menu_zirochka.json` у Room.
4. Замовлення створюються офлайн, зберігаються в локальній БД і пробують синхронізацію у мережі.
5. Оновлення меню: вкладка `Адмін` -> `Перечитати меню з assets`.

---

## Інструкція для персоналу

- Коротка інструкція: `docs/STAFF_GUIDE.md`
- Для друку: експортуйте `STAFF_GUIDE.md` у PDF (browser print або pandoc).
- Відеонавчання для команди: `canva-video-kit/`

---

## Вирішення проблем

- **Apps Script не відповідає**: перевірте деплой Web App, доступ `Anyone`, правильність URL.
- **Рядок не з'являється в Sheets**: перевірте лист `Бронювання` та логи Apps Script.
- **Telegram не надсилає**: перевірте `telegram.bot.token` і `telegram.chat.id`.
- **Меню не оновилось**: Admin -> `Перечитати меню з assets`.
- **Немає інтернету**: POS працює офлайн; повторіть синк після відновлення мережі.

---

## Структура репозиторію

- `app/` - Android POS (Kotlin, Compose, MVVM, Room, Retrofit, Hilt)
- `apps-script/` - Apps Script для бронювань (Instagram -> Sheets)
- `macros/` - інструкції для MacroDroid + `Code.gs` дубль
- `docs/` - staff guide та launch-документація
- `canva-video-kit/` - матеріали для відеоінструкції
- `verify_setup.sh` - передрелізна перевірка середовища

---

## Ліцензія
[MIT](LICENSE)
