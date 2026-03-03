/**
 * Code.gs — HTTP entry points and Apps Script trigger setup.
 *
 * Deploy this project as a Web App (Execute as: Me, Who has access: Anyone).
 * The resulting URL is your webhook endpoint.
 *
 * Supported POST actions (JSON body with "action" field):
 *   createBooking  — create a new booking
 *   updateStatus   — update booking status
 *   getBookings    — list all bookings
 *
 * GET ?action=ping  — health check
 */

/* global ContentService, ScriptApp, Logger */

// ---------------------------------------------------------------------------
// HTTP handlers
// ---------------------------------------------------------------------------

/**
 * Handles HTTP GET requests.
 * Supports: ?action=ping
 *
 * @param {GoogleAppsScript.Events.DoGet} e
 * @returns {GoogleAppsScript.Content.TextOutput}
 */
function doGet(e) {
  var action = e && e.parameter && e.parameter.action;

  if (action === 'ping') {
    return _jsonResponse({ ok: true, message: 'ZIROCHKABOT is running' });
  }

  return _jsonResponse({ ok: false, error: 'Unknown action. Use POST for booking operations.' }, 400);
}

/**
 * Handles HTTP POST requests.
 * Expects a JSON body: { "action": "createBooking"|"updateStatus"|"getBookings", ...fields }
 *
 * @param {GoogleAppsScript.Events.DoPost} e
 * @returns {GoogleAppsScript.Content.TextOutput}
 */
function doPost(e) {
  var cfg = getConfig();

  var raw;
  try {
    raw = JSON.parse(e.postData.contents);
  } catch (err) {
    return _jsonResponse({ ok: false, error: 'Invalid JSON body' }, 400);
  }

  var action = raw.action;

  if (action === 'createBooking') {
    return _handleCreateBooking(cfg, raw);
  }
  if (action === 'updateStatus') {
    return _handleUpdateStatus(cfg, raw);
  }
  if (action === 'getBookings') {
    return _handleGetBookings(cfg);
  }

  return _jsonResponse({ ok: false, error: 'Unknown action: ' + action }, 400);
}

// ---------------------------------------------------------------------------
// Action handlers
// ---------------------------------------------------------------------------

/**
 * Creates a booking, saves it to the sheet and notifies staff.
 *
 * @param {Object} cfg
 * @param {Object} raw - Parsed request body.
 * @returns {GoogleAppsScript.Content.TextOutput}
 */
function _handleCreateBooking(cfg, raw) {
  var parsed = parseBookingFromRequest(raw);
  if (!parsed.booking) {
    return _jsonResponse({ ok: false, errors: parsed.errors }, 422);
  }

  var booking = parsed.booking;
  appendBooking(cfg.spreadsheetId, cfg.sheetName, booking);

  var msg = buildNewBookingMessage(booking);
  notifyAll(cfg.telegramBotToken, cfg.ownerChatId, cfg.staffChatIds, msg);

  Logger.log('Booking created: ' + booking.id);
  return _jsonResponse({ ok: true, id: booking.id });
}

/**
 * Updates the status of an existing booking and notifies staff.
 *
 * @param {Object} cfg
 * @param {Object} raw - Must contain { id, status }.
 * @returns {GoogleAppsScript.Content.TextOutput}
 */
function _handleUpdateStatus(cfg, raw) {
  if (!raw.id || !raw.status) {
    return _jsonResponse({ ok: false, error: 'id and status are required' }, 422);
  }

  var found = findBookingById(cfg.spreadsheetId, cfg.sheetName, raw.id);
  if (!found) {
    return _jsonResponse({ ok: false, error: 'Booking not found: ' + raw.id }, 404);
  }

  var oldStatus      = found.booking.status;
  found.booking.status  = raw.status;
  updateBooking(cfg.spreadsheetId, cfg.sheetName, found.booking);

  var msg = buildStatusChangeMessage(found.booking, oldStatus);
  notifyAll(cfg.telegramBotToken, cfg.ownerChatId, cfg.staffChatIds, msg);

  Logger.log('Booking ' + raw.id + ' status: ' + oldStatus + ' → ' + raw.status);
  return _jsonResponse({ ok: true });
}

/**
 * Returns all bookings as a JSON array.
 *
 * @param {Object} cfg
 * @returns {GoogleAppsScript.Content.TextOutput}
 */
function _handleGetBookings(cfg) {
  var bookings = getAllBookings(cfg.spreadsheetId, cfg.sheetName);
  // Convert Date fields to ISO strings for JSON serialisation
  var serialised = bookings.map(function (b) {
    return bookingToRow(b).reduce(function (acc, val, i) {
      acc[SHEET_COLUMNS[i]] = val;
      return acc;
    }, {});
  });
  return _jsonResponse({ ok: true, bookings: serialised });
}

// ---------------------------------------------------------------------------
// Trigger setup
// ---------------------------------------------------------------------------

/**
 * Creates a time-based trigger to run the reminder job every 10 minutes.
 * Run this once manually from the Apps Script editor (Run → setupTriggers).
 */
function setupTriggers() {
  // Remove stale triggers to avoid duplicates
  ScriptApp.getProjectTriggers().forEach(function (t) {
    if (t.getHandlerFunction() === 'runReminderJob') {
      ScriptApp.deleteTrigger(t);
    }
  });

  ScriptApp.newTrigger('runReminderJob')
    .timeBased()
    .everyMinutes(10)
    .create();

  Logger.log('Reminder trigger set: every 10 minutes.');
}

/**
 * Time-triggered entry point for the reminder job.
 * Do not rename — referenced by setupTriggers().
 */
function runReminderJob() {
  var cfg    = getConfig();
  var result = processReminders(cfg);
  Logger.log('Reminder job: sent=' + result.sent + ', failed=' + result.failed);
}

// ---------------------------------------------------------------------------
// Private helpers
// ---------------------------------------------------------------------------

/**
 * Returns a JSON ContentService response.
 *
 * @param {Object} data
 * @param {number} [_statusCode] - Ignored (GAS always returns 200); kept for documentation.
 * @returns {GoogleAppsScript.Content.TextOutput}
 */
function _jsonResponse(data, _statusCode) {
  return ContentService
    .createTextOutput(JSON.stringify(data))
    .setMimeType(ContentService.MimeType.JSON);
}
