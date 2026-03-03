const SHEET_NAME = "Бронювання";
const HEADERS = ["Дата", "Instagram", "Повідомлення", "OrderSummary", "Total"];

function doGet() {
  try {
    const sheet = getBookingSheet_();
    return json_({
      status: "ok",
      sheet: SHEET_NAME,
      rows: Math.max(sheet.getLastRow() - 1, 0),
      timestamp: new Date().toISOString(),
    });
  } catch (err) {
    Logger.log("Error in doGet: " + err);
    return json_({ status: "error", message: String(err) });
  }
}

function doPost(e) {
  try {
    const rawBody = e && e.postData ? e.postData.contents : "{}";
    const data = parseBody_(rawBody);
    const username = data.username || data.instagram_username || "";
    const message = data.message || data.text || "";
    const orderSummary = data.orderSummary || data.order_summary || "";
    const total = data.total || "";

    const sheet = getBookingSheet_();
    sheet.appendRow([new Date(), username, message, orderSummary, total]);

    return json_({ status: "ok", saved: true });
  } catch (err) {
    Logger.log("Error in doPost: " + err);
    return json_({ status: "error", message: String(err) });
  }
}

function parseBody_(rawBody) {
  try {
    return JSON.parse(rawBody);
  } catch (err) {
    return { message: rawBody };
  }
}

function getBookingSheet_() {
  const ss = SpreadsheetApp.getActiveSpreadsheet();
  let sheet = ss.getSheetByName(SHEET_NAME);
  if (!sheet) {
    sheet = ss.insertSheet(SHEET_NAME);
  }

  const firstRow = sheet.getRange(1, 1, 1, HEADERS.length).getValues()[0];
  const hasHeaders = HEADERS.every((header, index) => firstRow[index] === header);
  if (!hasHeaders) {
    sheet.getRange(1, 1, 1, HEADERS.length).setValues([HEADERS]);
  }
  return sheet;
}

function json_(payload) {
  return ContentService.createTextOutput(JSON.stringify(payload)).setMimeType(
    ContentService.MimeType.JSON
  );
}
