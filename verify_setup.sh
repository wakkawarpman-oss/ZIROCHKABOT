#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

REQUIRED_FILES=(
  "$ROOT/app/build.gradle.kts"
  "$ROOT/app/src/main/AndroidManifest.xml"
  "$ROOT/app/src/main/assets/menu_zirochka.json"
  "$ROOT/macros/Code.gs"
  "$ROOT/README.md"
  "$ROOT/docs/STAFF_GUIDE.md"
)

missing=()
for f in "${REQUIRED_FILES[@]}"; do
  [[ -f "$f" ]] || missing+=("$f")
done

if [[ ${#missing[@]} -gt 0 ]]; then
  echo "❌ Відсутні файли:"
  printf ' - %s\n' "${missing[@]}"
else
  echo "✅ Всі необхідні файли на місці"
fi

echo "---- Перевірка інтернет-з'єднання ----"
# Використайте змінну PING_HOST для альтернативної адреси, якщо 8.8.8.8 недоступний
PING_HOST="${PING_HOST:-8.8.8.8}"
# -W2: таймаут відповіді у секундах для більшості Linux (за потреби замініть на -w або -t на macOS)
if ping -c1 -W2 "$PING_HOST" >/dev/null 2>&1; then
    echo "✅ Інтернет доступний"
else
    echo "⚠️  Інтернет недоступний або заблокований"
fi

APP_SCRIPT_URL="${1:-${SHEETS_SCRIPT_URL:-}}"
if [[ -n "$APP_SCRIPT_URL" ]]; then
  echo "---- Перевірка Apps Script ----"
  if curl -s --max-time 5 -I "$APP_SCRIPT_URL" >/dev/null; then
    echo "✅ Apps Script URL відповідає"
  else
    echo "⚠️  Apps Script не відповідає або URL порожній"
  fi
else
  echo "ℹ️  Передайте URL Apps Script як аргумент або встановіть змінну SHEETS_SCRIPT_URL"
fi

echo "---- Перевірка MacroDroid (через ADB, опціонально) ----"
if command -v adb >/dev/null 2>&1; then
  if adb get-state >/dev/null 2>&1; then
    if adb shell pm list packages | grep -q "com.arlosoft.macrodroid"; then
      echo "✅ MacroDroid встановлено на підключеному пристрої"
    else
      echo "⚠️  MacroDroid не знайдено на підключеному пристрої"
    fi
  else
    echo "ℹ️  Пристрій не підключено через ADB"
  fi
else
  echo "ℹ️  ADB не встановлено — пропускаємо перевірку MacroDroid"
fi

echo "---- Підсумок ----"
if [[ ${#missing[@]} -eq 0 ]]; then
  echo "Готовність: базова структура OK. Налаштуйте токени та URL перед релізом."
else
  echo "Готовність: необхідно додати відсутні файли."
fi
