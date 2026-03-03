/**
 * SheetManager.gs — Google Sheets CRUD for bookings.
 *
 * Sheet layout: one header row followed by data rows whose column order is
 * defined by SHEET_COLUMNS in Booking.gs.
 */

/* global SpreadsheetApp */

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Ensures the "Bookings" sheet exists and has a header row.
 * Safe to call on every deploy / first run.
 *
 * @param {string} spreadsheetId
 * @param {string} sheetName
 * @returns {GoogleAppsScript.Spreadsheet.Sheet}
 */
function ensureSheet(spreadsheetId, sheetName) {
  var ss    = SpreadsheetApp.openById(spreadsheetId);
  var sheet = ss.getSheetByName(sheetName);
  if (!sheet) {
    sheet = ss.insertSheet(sheetName);
    sheet.appendRow(SHEET_COLUMNS); // header
    sheet.setFrozenRows(1);
  }
  return sheet;
}

/**
 * Appends a new booking row.
 *
 * @param {string}  spreadsheetId
 * @param {string}  sheetName
 * @param {Booking} booking
 */
function appendBooking(spreadsheetId, sheetName, booking) {
  var sheet = ensureSheet(spreadsheetId, sheetName);
  sheet.appendRow(bookingToRow(booking));
}

/**
 * Returns all bookings from the sheet (excludes the header row).
 *
 * @param {string} spreadsheetId
 * @param {string} sheetName
 * @returns {Booking[]}
 */
function getAllBookings(spreadsheetId, sheetName) {
  var sheet     = ensureSheet(spreadsheetId, sheetName);
  var lastRow   = sheet.getLastRow();
  if (lastRow < 2) return [];
  var rows = sheet.getRange(2, 1, lastRow - 1, SHEET_COLUMNS.length).getValues();
  return rows
    .filter(function (row) { return row[0]; }) // skip completely empty rows
    .map(function (row) { return bookingFromRow(row); });
}

/**
 * Finds a booking row by its ID.
 * Returns { rowIndex, booking } or null if not found.
 * rowIndex is 1-based (sheet row number).
 *
 * @param {string} spreadsheetId
 * @param {string} sheetName
 * @param {string} bookingId
 * @returns {{ rowIndex: number, booking: Booking }|null}
 */
function findBookingById(spreadsheetId, sheetName, bookingId) {
  var sheet   = ensureSheet(spreadsheetId, sheetName);
  var lastRow = sheet.getLastRow();
  if (lastRow < 2) return null;

  var idColValues = sheet.getRange(2, 1, lastRow - 1, 1).getValues();
  for (var i = 0; i < idColValues.length; i++) {
    if (String(idColValues[i][0]) === bookingId) {
      var row     = sheet.getRange(i + 2, 1, 1, SHEET_COLUMNS.length).getValues()[0];
      return { rowIndex: i + 2, booking: bookingFromRow(row) };
    }
  }
  return null;
}

/**
 * Overwrites the sheet row for the given booking.
 * Returns true if the booking was found and updated, false otherwise.
 *
 * @param {string}  spreadsheetId
 * @param {string}  sheetName
 * @param {Booking} booking
 * @returns {boolean}
 */
function updateBooking(spreadsheetId, sheetName, booking) {
  var found = findBookingById(spreadsheetId, sheetName, booking.id);
  if (!found) return false;
  var sheet = SpreadsheetApp.openById(spreadsheetId).getSheetByName(sheetName);
  sheet.getRange(found.rowIndex, 1, 1, SHEET_COLUMNS.length).setValues([bookingToRow(booking)]);
  return true;
}

/**
 * Returns all CONFIRMED bookings that still need a reminder.
 * (reminderSent === false AND bookingTime in the future)
 *
 * @param {string} spreadsheetId
 * @param {string} sheetName
 * @param {number} reminderMinutesBefore
 * @param {Date}   [now]
 * @returns {Booking[]}
 */
function getPendingReminderBookings(spreadsheetId, sheetName, reminderMinutesBefore, now) {
  var all = getAllBookings(spreadsheetId, sheetName);
  return all.filter(function (b) {
    return shouldSendReminder(b, reminderMinutesBefore, now);
  });
}
