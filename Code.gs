/**
 * Append Instagram (or any POSTed) data to a Google Sheet.
 * Deploy this script as a Web App with access for Anyone to allow MacroDroid to call it.
 */
// Змініть назву аркуша, якщо хочете писати дані в іншу вкладку.
const SHEET_NAME = 'InstagramToSheets';

function doPost(e) {
  if (!e || !e.postData || !e.postData.contents) {
    return ContentService.createTextOutput('Missing payload');
  }

  let payload;
  try {
    payload = JSON.parse(e.postData.contents);
  } catch (err) {
    return ContentService.createTextOutput('Invalid JSON: ' + err.message);
  }

  const sheet = getOrCreateSheet_(SHEET_NAME);
  const row = [
    new Date(),
    payload.username || '',
    payload.fullName || '',
    payload.message || '',
    payload.profileUrl || '',
    payload.source || 'instagram',
  ];

  sheet.appendRow(row);
  return ContentService.createTextOutput('OK');
}

function getOrCreateSheet_(name) {
  const ss = SpreadsheetApp.getActive();
  return ss.getSheetByName(name) || ss.insertSheet(name);
}
