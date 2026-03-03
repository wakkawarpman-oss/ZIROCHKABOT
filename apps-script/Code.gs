/**
 * ZIROCHKABOT booking webhook for Google Sheets.
 *
 * Deploy as Web App:
 * - Execute as: Me
 * - Who has access: Anyone
 */

const CONFIG = {
  spreadsheetId: "", // optional: leave empty to use active spreadsheet
  sheetName: "Bronyuvannya",
  timezone: "Europe/Kyiv",
};

const HEADERS = [
  "created_at",
  "source",
  "instagram_username",
  "guest_name",
  "phone",
  "guests",
  "visit_date",
  "visit_time",
  "message",
  "raw_payload",
];

function doGet() {
  return response_({
    ok: true,
    service: "ZIROCHKABOT booking webhook",
    status: "healthy",
    timestamp: now_(),
  });
}

function doPost(e) {
  try {
    const payload = parsePayload_(e);
    const row = normalizePayload_(payload);
    const sheet = getOrCreateSheet_();
    ensureHeaders_(sheet);
    sheet.appendRow(row);

    return response_({
      ok: true,
      saved: true,
      timestamp: row[0],
      reply:
        "Dyakuiemo. Vashu zayavku na bronyuvannya otrymano. My pidtverdymo detalі nayblyzhchym chasom.",
    });
  } catch (error) {
    return response_({
      ok: false,
      error: String(error),
    });
  }
}

function parsePayload_(e) {
  if (!e || !e.postData || !e.postData.contents) {
    return {};
  }

  const raw = e.postData.contents;
  try {
    return JSON.parse(raw);
  } catch (err) {
    // If macro sends x-www-form-urlencoded or plain text, keep it in message.
    return { message: raw };
  }
}

function normalizePayload_(payload) {
  const ts = now_();
  const source = firstDefined_(payload.source, "instagram_dm");
  const username = firstDefined_(payload.instagram_username, payload.username, "");
  const guestName = firstDefined_(payload.guest_name, payload.name, "");
  const phone = firstDefined_(payload.phone, payload.phone_number, "");
  const guests = firstDefined_(payload.guests, payload.people, "");
  const visitDate = firstDefined_(payload.visit_date, payload.date, "");
  const visitTime = firstDefined_(payload.visit_time, payload.time, "");
  const message = firstDefined_(payload.message, payload.text, "");
  const raw = JSON.stringify(payload);

  return [ts, source, username, guestName, phone, guests, visitDate, visitTime, message, raw];
}

function getOrCreateSheet_() {
  let spreadsheet;
  if (CONFIG.spreadsheetId) {
    spreadsheet = SpreadsheetApp.openById(CONFIG.spreadsheetId);
  } else {
    spreadsheet = SpreadsheetApp.getActiveSpreadsheet();
  }

  let sheet = spreadsheet.getSheetByName(CONFIG.sheetName);
  if (!sheet) {
    sheet = spreadsheet.insertSheet(CONFIG.sheetName);
  }
  return sheet;
}

function ensureHeaders_(sheet) {
  const firstRow = sheet.getRange(1, 1, 1, HEADERS.length).getValues()[0];
  const hasHeaders = HEADERS.every((header, index) => firstRow[index] === header);
  if (!hasHeaders) {
    sheet.getRange(1, 1, 1, HEADERS.length).setValues([HEADERS]);
  }
}

function now_() {
  return Utilities.formatDate(new Date(), CONFIG.timezone, "yyyy-MM-dd HH:mm:ss");
}

function firstDefined_() {
  for (let i = 0; i < arguments.length; i += 1) {
    const value = arguments[i];
    if (value !== undefined && value !== null && value !== "") {
      return value;
    }
  }
  return "";
}

function response_(obj) {
  return ContentService.createTextOutput(JSON.stringify(obj)).setMimeType(
    ContentService.MimeType.JSON
  );
}
