/**
 * Google Apps Script для ресторану «Зірочка»
 * Приймає POST-запити від MacroDroid і додає бронювання в Google Sheets.
 */

const SHEET_NAME = 'Бронювання'; // Назва сторінки в таблиці

function doPost(e) {
  try {
    // Отримуємо дані з MacroDroid
    const data = JSON.parse(e.postData.contents);
    const username = data.username || 'Гість';
    const message = data.message || '';
    const timestamp = new Date();

    // Відкриваємо таблицю і сторінку
    const sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(SHEET_NAME);
    if (!sheet) {
      throw new Error(`Сторінка "${SHEET_NAME}" не знайдена. Створіть її.`);
    }

    // Додаємо рядок: дата, ім'я, повідомлення
    sheet.appendRow([timestamp, username, message]);

    return ContentService
      .createTextOutput(JSON.stringify({ result: 'success', restaurant: 'Зірочка' }))
      .setMimeType(ContentService.MimeType.JSON);
  } catch (error) {
    // Обмежуйте доступ до логів через права у проєкті Apps Script (Share → лише адміністратори; деталі в README, розділ «Крок 1»).
    console.error('Помилка під час обробки бронювання:', error);
    return ContentService
      .createTextOutput(JSON.stringify({ result: 'error', error: 'internal_error' }))
      .setMimeType(ContentService.MimeType.JSON);
  }
}

// Для перевірки роботи
function doGet() {
  return ContentService
    .createTextOutput('Скрипт «Зірочка» працює. Використовуйте POST для відправки бронювань.');
}
