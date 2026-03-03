# ZIROCHKABOT

Два компоненти для ресторану "Зірочка":

- ✅ Instagram-бот для бронювань (Google Apps Script + MacroDroid).
- 🧾 POS-компонент для замовлень (Jetpack Compose + Room + Retrofit) — див. `pos/README.md`.

Базове меню доступне у `menu_zirochka.json`.

На планшеті підтримуються два режими:
- **Instagram** — MacroDroid працює у фоні, приймає заявки та відповідає за скриптом.
- **POS** — окремий застосунок для офіціантів; при сповіщеннях Instagram MacroDroid можна вивести поверх, після відповіді повернутися до POS.
