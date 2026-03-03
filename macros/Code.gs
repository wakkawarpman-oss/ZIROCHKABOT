function doPost(e) {
  try {
    var body = e && e.postData ? e.postData.contents : '{}';
    var data = JSON.parse(body);
    var username = data.username || '';
    var message = data.message || '';
    var sheet = getBookingSheet();
    sheet.appendRow([new Date(), username, message]);
    return ContentService
      .createTextOutput(JSON.stringify({ status: 'ok' }))
      .setMimeType(ContentService.MimeType.JSON);
  } catch (err) {
    Logger.log('Error in doPost: ' + err);
    return ContentService
      .createTextOutput(JSON.stringify({ status: 'error', message: err.toString() }))
      .setMimeType(ContentService.MimeType.JSON);
  }
}

function doGet() {
  try {
    var sheet = getBookingSheet();
    var last = sheet.getLastRow();
    return ContentService
      .createTextOutput(JSON.stringify({ status: 'ok', lastRow: last }))
      .setMimeType(ContentService.MimeType.JSON);
  } catch (err) {
    Logger.log('Error in doGet: ' + err);
    return ContentService
      .createTextOutput(JSON.stringify({ status: 'error', message: err.toString() }))
      .setMimeType(ContentService.MimeType.JSON);
  }
}

function getBookingSheet() {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName('Бронювання');
  if (!sheet) {
    sheet = ss.insertSheet('Бронювання');
    sheet.appendRow(['Дата', 'Instagram', 'Повідомлення']);
  }
  return sheet;
}
