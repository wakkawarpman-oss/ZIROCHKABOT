/**
 * Google Apps Script для прийому POST-запитів від MacroDroid
 * і додавання рядків у Google Sheets.
 * 
 * Налаштування:
 * - Змініть SHEET_NAME, якщо ваш лист називається інакше.
 * - Розгорніть як веб-додаток з доступом "Усі".
 */

const SHEET_NAME = 'Бронювання'; // Назва аркуша в таблиці
const DEFAULT_USERNAME = 'Невідомо';

class AppError extends Error {
  constructor(message, code) {
    super(message);
    this.code = code;
  }
}

function mapErrorToClientMessage(error) {
  if (error instanceof AppError) {
    return { message: error.message, code: error.code };
  }
  if (error instanceof SyntaxError) {
    return { message: 'Невірний формат JSON', code: 'INVALID_JSON' };
  }
  return { message: 'Внутрішня помилка', code: 'UNKNOWN_ERROR' };
}

function doPost(e) {
  try {
    // Отримуємо тіло запиту (очікується JSON)
    if (!e || !e.postData || !e.postData.contents) {
      throw new AppError('Порожнє тіло запиту', 'EMPTY_BODY');
    }
    const data = JSON.parse(e.postData.contents);
    const username = data.username || DEFAULT_USERNAME;
    const message = data.message || '';
    const timestamp = new Date();

    // Отримуємо активну таблицю і потрібний лист
    const sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(SHEET_NAME);
    if (!sheet) {
      throw new AppError('Аркуш не знайдено', 'SHEET_NOT_FOUND');
    }

    // Додаємо рядок: дата, користувач, повідомлення
    sheet.appendRow([timestamp, username, message]);

    // Повертаємо успішну відповідь
    return ContentService
      .createTextOutput(JSON.stringify({ result: 'success' }))
      .setMimeType(ContentService.MimeType.JSON);
  } catch (error) {
    // Логування помилки (можна переглянути у Виконанні -> Журнали)
    const clientError = mapErrorToClientMessage(error);
    console.error('Request processing failed:', clientError.code, clientError.message);
    return ContentService
      .createTextOutput(JSON.stringify({ result: 'error', error: clientError.message, code: clientError.code }))
      .setMimeType(ContentService.MimeType.JSON);
  }
}

// Для тестування GET-запитом
function doGet() {
  return ContentService
    .createTextOutput('Скрипт працює. Використовуйте POST для відправки даних.')
    .setMimeType(ContentService.MimeType.TEXT);
}
