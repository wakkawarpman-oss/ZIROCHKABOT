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

    // Логування для перевірки
    console.log(`Додано бронювання від ${username} о ${timestamp.toISOString()}`);

    return ContentService
      .createTextOutput(JSON.stringify({ result: 'success', restaurant: 'Зірочка' }))
      .setMimeType(ContentService.MimeType.JSON);
  } catch (error) {
    console.error('Помилка:', error.message);
    return ContentService
      .createTextOutput(JSON.stringify({ result: 'error', error: error.toString() }))
      .setMimeType(ContentService.MimeType.JSON);
  }
}

// Для перевірки роботи
function doGet() {
  return ContentService
    .createTextOutput('Скрипт «Зірочка» працює. Використовуйте POST для відправки бронювань.');
}
