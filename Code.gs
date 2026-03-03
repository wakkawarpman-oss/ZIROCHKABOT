const SHEET_NAME = 'Бронювання';

function doPost(e) {
  console.log('Incoming request to Зірочка bot', {
    timestamp: new Date(),
    hasPostData: !!(e && e.postData),
  });
  try {
    if (!e || !e.postData || !e.postData.contents) {
      throw new Error('Missing postData');
    }
    const rawBody = e.postData.contents;
    let payload = {};
    try {
      payload = JSON.parse(rawBody);
    } catch (parseError) {
      console.error('Failed to parse incoming JSON', rawBody, parseError);
      throw parseError;
    }
    const username = payload.username || '';
    const message = payload.message || '';
    const source = payload.source || 'Instagram';

    const spreadsheet = SpreadsheetApp.getActiveSpreadsheet();
    if (!spreadsheet) {
      throw new Error('Не знайдено активну таблицю. Прив’яжіть скрипт до потрібної Google Sheets.');
    }
    const sheet = spreadsheet.getSheetByName(SHEET_NAME);
    if (!sheet) {
      throw new Error(`Лист не знайдено: ${SHEET_NAME}. Перевірте, що назва листа збігається, включно з кириличними літерами та пробілами.`);
    }
    sheet.appendRow([new Date(), username, message, source]);

    const output = { result: 'success', restaurant: 'Зірочка' };
    return ContentService
      .createTextOutput(JSON.stringify(output))
      .setMimeType(ContentService.MimeType.JSON);
  } catch (error) {
    console.error('Error in doPost', error);
    const output = { result: 'error', restaurant: 'Зірочка' };
    return ContentService
      .createTextOutput(JSON.stringify(output))
      .setMimeType(ContentService.MimeType.JSON);
  }
}
