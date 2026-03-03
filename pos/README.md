# POS-компонент "Зірочка"

Базовий скелет POS для планшета з Jetpack Compose, Room та Retrofit. Файли зберігаються локально і можуть бути розгорнуті в Android-проєкті.

## Структура
- `menu_zirochka.json` — цифрове меню з 9 категоріями.
- `data/model` — DTO для меню та замовлень.
- `data/local` — Room сутності, DAO, база.
- `data/remote` — Retrofit API для Google Sheets і Telegram Bot.
- `ui/components` — Compose-компоненти (категорії, сітка, кошик, діалог).
- `ui/viewmodel` — логіка меню, кошика та відправки замовлень.
- `ZirochkaApp.kt` — точка входу та завантаження меню з JSON.
- `ui_mock.png` — ескізний макет інтерфейсу (категорії + сітка + кошик).

## Підключення в Android-проєкті
1. Додайте залежності: Room, Retrofit, Moshi/Serialization, Compose, Coroutines, Hilt/DataStore за потреби.
2. Скопіюйте каталог `pos/src/main/kotlin` у свій `app/src/main/kotlin`.
3. Покладіть `menu_zirochka.json` до `assets/` та використовуйте `loadCategoriesFromJson`.
4. Ініціалізуйте `AppDatabase` через Room, передайте DAO та сервіси у ViewModel (Hilt).
5. Використовуйте `PosApp` у `setContent` вашої Activity.

## Налаштування API
- **Google Sheets**: сервісний акаунт, ввімкнений API, передайте базовий URL на кшталт `https://sheets.googleapis.com/v4/spreadsheets/{SPREADSHEET_ID}/`.
- **Telegram**: базовий URL `https://api.telegram.org/bot{TOKEN}/`, змінна `chatId` для кухні/власника.

## Режим офлайн
- Замовлення зберігаються у Room.
- Відправка в Telegram/Sheets може бути викликана при синхронізації, окремий воркер можна додати пізніше.

## Тестування
- ViewModel-и використовують чисту логіку і можуть тестуватися з фейковими DAO/сервісами.
- JSON можна валідовувати через `Json { ignoreUnknownKeys = true }`.
