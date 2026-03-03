#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

REQUIRED_FILES=(
  "$ROOT/README.md"
  "$ROOT/build.gradle.kts"
  "$ROOT/settings.gradle.kts"
  "$ROOT/app/build.gradle.kts"
  "$ROOT/app/src/main/AndroidManifest.xml"
  "$ROOT/app/src/main/assets/menu_zirochka.json"
  "$ROOT/apps-script/Code.gs"
  "$ROOT/macros/Code.gs"
  "$ROOT/macros/MACRODROID.md"
  "$ROOT/docs/STAFF_GUIDE.md"
  "$ROOT/docs/launch/go-live-checklist.md"
)

errors=()
warnings=()
for f in "${REQUIRED_FILES[@]}"; do
  [[ -f "$f" ]] || errors+=("Відсутній файл: $f")
done

echo "---- Перевірка файлів ----"
if [[ ${#errors[@]} -eq 0 ]]; then
  echo "✅ Всі необхідні файли на місці"
fi

echo "---- Перевірка інтернет-з'єднання ----"
if curl -s --max-time 5 https://www.google.com >/dev/null 2>&1; then
    echo "✅ Інтернет доступний"
else
    errors+=("Немає доступу до інтернету")
fi

APP_SCRIPT_URL="${1:-${SHEETS_SCRIPT_URL:-}}"
echo "---- Перевірка Apps Script URL ----"
if [[ -n "$APP_SCRIPT_URL" ]]; then
  if curl -s --max-time 7 "$APP_SCRIPT_URL" >/dev/null; then
    echo "✅ Apps Script URL відповідає"
  else
    errors+=("Apps Script URL недоступний: $APP_SCRIPT_URL")
  fi
else
  warnings+=("Не вказано Apps Script URL (аргумент або SHEETS_SCRIPT_URL)")
fi

echo "---- Перевірка MacroDroid (через ADB, опціонально) ----"
if command -v adb >/dev/null 2>&1; then
  if adb get-state >/dev/null 2>&1; then
    PKGS="$(adb shell pm list packages | tr -d '\r')"
    if [[ "$PKGS" == *"com.arlosoft.macrodroid"* ]]; then
      echo "✅ MacroDroid встановлено на підключеному пристрої"
    else
      warnings+=("MacroDroid не знайдено на підключеному пристрої")
    fi
  else
    warnings+=("Пристрій не підключено через ADB")
  fi
else
  warnings+=("ADB не встановлено, перевірка MacroDroid пропущена")
fi

echo "---- Підсумок ----"
if [[ ${#warnings[@]} -gt 0 ]]; then
  echo "Попередження:"
  printf ' - %s\n' "${warnings[@]}"
fi

if [[ ${#errors[@]} -eq 0 ]]; then
  echo "✅ Все готово"
else
  echo "❌ Виявлені помилки:"
  printf ' - %s\n' "${errors[@]}"
  exit 1
fi
