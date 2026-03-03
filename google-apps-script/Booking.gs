/**
 * Booking.gs — Booking data model, factory, validation and business helpers.
 *
 * All business logic that does NOT depend on external services lives here so
 * it can be unit-tested without mocking Google/Telegram APIs.
 */

/** Allowed booking status values. */
var BookingStatus = {
  PENDING:   'pending',
  CONFIRMED: 'confirmed',
  CANCELLED: 'cancelled',
  COMPLETED: 'completed'
};

/**
 * Column order in the Google Sheets "Bookings" sheet (1-based index → 0-based array).
 * Changing the order here is enough to keep the rest of the code in sync.
 */
var SHEET_COLUMNS = [
  'id',               // A
  'clientName',       // B
  'clientPhone',      // C
  'clientTelegramId', // D
  'guestCount',       // E
  'bookingTime',      // F  (ISO-8601 string, stored/read as string for portability)
  'tableNumber',      // G
  'notes',            // H
  'status',           // I
  'reminderSent',     // J  ('true'/'false')
  'createdAt',        // K
  'updatedAt'         // L
];

// ---------------------------------------------------------------------------
// Factory & conversion
// ---------------------------------------------------------------------------

/**
 * Creates a Booking object from a raw data hash.
 * Unknown keys are ignored; missing optional keys get safe defaults.
 *
 * @param {Object} data - Raw key/value map (e.g. parsed JSON or sheet row).
 * @param {function(): string} [idGen] - Optional ID generator (injectable for testing).
 * @returns {Booking}
 *
 * @typedef {{
 *   id: string,
 *   clientName: string,
 *   clientPhone: string,
 *   clientTelegramId: string,
 *   guestCount: number,
 *   bookingTime: Date,
 *   tableNumber: string,
 *   notes: string,
 *   status: string,
 *   reminderSent: boolean,
 *   createdAt: Date,
 *   updatedAt: Date
 * }} Booking
 */
function createBooking(data, idGen) {
  var now = new Date();
  var generateFn = idGen || _defaultIdGen;

  return {
    id:               data.id ? String(data.id) : generateFn(),
    clientName:       String(data.clientName    || '').trim(),
    clientPhone:      String(data.clientPhone   || '').trim(),
    clientTelegramId: String(data.clientTelegramId || '').trim(),
    guestCount:       Math.max(1, parseInt(data.guestCount, 10) || 1),
    bookingTime:      _parseDate(data.bookingTime),
    tableNumber:      String(data.tableNumber   || '').trim(),
    notes:            String(data.notes         || '').trim(),
    status:           _oneOf(data.status, BookingStatus, BookingStatus.PENDING),
    reminderSent:     data.reminderSent === true || data.reminderSent === 'true',
    createdAt:        data.createdAt ? _parseDate(data.createdAt) : now,
    updatedAt:        now
  };
}

/**
 * Converts a Booking object to a flat array matching SHEET_COLUMNS order.
 *
 * @param {Booking} booking
 * @returns {Array}
 */
function bookingToRow(booking) {
  return SHEET_COLUMNS.map(function (col) {
    var val = booking[col];
    if (val instanceof Date) return val.toISOString();
    if (typeof val === 'boolean') return String(val);
    return val !== undefined && val !== null ? String(val) : '';
  });
}

/**
 * Reconstructs a Booking object from a sheet row array.
 *
 * @param {Array} row
 * @returns {Booking}
 */
function bookingFromRow(row) {
  var data = {};
  SHEET_COLUMNS.forEach(function (col, i) {
    data[col] = row[i];
  });
  return createBooking(data, function () { return data.id || ''; });
}

// ---------------------------------------------------------------------------
// Validation
// ---------------------------------------------------------------------------

/**
 * Validates a Booking object.
 *
 * @param {Booking} booking
 * @returns {{ valid: boolean, errors: string[] }}
 */
function validateBooking(booking) {
  var errors = [];

  if (!booking.clientName) {
    errors.push('clientName is required');
  }
  if (!booking.clientPhone && !booking.clientTelegramId) {
    errors.push('clientPhone or clientTelegramId is required');
  }
  if (!booking.bookingTime || isNaN(booking.bookingTime.getTime())) {
    errors.push('bookingTime must be a valid date/time');
  }
  if (booking.guestCount < 1) {
    errors.push('guestCount must be at least 1');
  }

  return { valid: errors.length === 0, errors: errors };
}

/**
 * Parses a booking from a raw HTTP request body object.
 *
 * @param {Object} raw
 * @param {function(): string} [idGen]
 * @returns {{ booking: Booking|null, errors: string[] }}
 */
function parseBookingFromRequest(raw, idGen) {
  if (!raw || typeof raw !== 'object') {
    return { booking: null, errors: ['Invalid or missing request body'] };
  }
  var booking = createBooking(raw, idGen);
  var result  = validateBooking(booking);
  if (!result.valid) return { booking: null, errors: result.errors };
  return { booking: booking, errors: [] };
}

// ---------------------------------------------------------------------------
// Business helpers (pure — no external I/O)
// ---------------------------------------------------------------------------

/**
 * Calculates how many minutes remain until the booking starts.
 * Returns a negative number if the booking is in the past.
 *
 * @param {Booking} booking
 * @param {Date} [now] - Optional reference time (defaults to current time).
 * @returns {number}
 */
function minutesUntilBooking(booking, now) {
  var reference = now instanceof Date ? now : new Date();
  return Math.round((booking.bookingTime.getTime() - reference.getTime()) / 60000);
}

/**
 * Returns true when a reminder should be sent for this booking.
 * Conditions:
 *  - status is CONFIRMED
 *  - reminder has not been sent yet
 *  - remaining time is between 0 and reminderMinutesBefore (inclusive)
 *
 * @param {Booking} booking
 * @param {number}  reminderMinutesBefore - Config value (e.g. 60).
 * @param {Date}    [now]
 * @returns {boolean}
 */
function shouldSendReminder(booking, reminderMinutesBefore, now) {
  if (booking.status !== BookingStatus.CONFIRMED) return false;
  if (booking.reminderSent) return false;
  var mins = minutesUntilBooking(booking, now);
  return mins >= 0 && mins <= reminderMinutesBefore;
}

// ---------------------------------------------------------------------------
// Private helpers
// ---------------------------------------------------------------------------

/** @returns {string} */
function _defaultIdGen() {
  /* global Utilities */
  return Utilities.getUuid();
}

/**
 * @param {*} value
 * @returns {Date}
 */
function _parseDate(value) {
  if (value instanceof Date) return value;
  if (!value) return new Date(NaN);
  return new Date(value);
}

/**
 * Returns `value` if it is one of the allowed enum values, otherwise returns `fallback`.
 *
 * @param {*}      value
 * @param {Object} enumObj
 * @param {*}      fallback
 * @returns {*}
 */
function _oneOf(value, enumObj, fallback) {
  var allowed = Object.keys(enumObj).map(function (k) { return enumObj[k]; });
  return allowed.indexOf(value) !== -1 ? value : fallback;
}
