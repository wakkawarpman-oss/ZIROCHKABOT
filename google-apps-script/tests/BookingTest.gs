/**
 * tests/BookingTest.gs — Unit tests for Booking.gs business logic.
 *
 * Run from the Apps Script editor: open this file and run `runBookingTests()`.
 * A summary is printed to Logger (View → Logs).
 *
 * No external services are called — all tests use pure functions only.
 */

/* global Logger */

// ---------------------------------------------------------------------------
// Test runner (minimal, no dependencies)
// ---------------------------------------------------------------------------

/**
 * @param {string}   suiteName
 * @param {Function} fn  - Function that calls assert* helpers.
 */
function _suite(suiteName, fn) {
  try {
    fn();
    Logger.log('✅ PASS  ' + suiteName);
  } catch (e) {
    Logger.log('❌ FAIL  ' + suiteName + '\n       ' + e.message);
  }
}

/** @param {boolean} condition @param {string} [msg] */
function _assert(condition, msg) {
  if (!condition) throw new Error('Assertion failed: ' + (msg || ''));
}

/** @param {*} actual @param {*} expected @param {string} [msg] */
function _assertEqual(actual, expected, msg) {
  if (actual !== expected) {
    throw new Error(
      (msg ? msg + ': ' : '') +
      'expected ' + JSON.stringify(expected) + ' but got ' + JSON.stringify(actual)
    );
  }
}

// ---------------------------------------------------------------------------
// Test entry point — run all booking tests
// ---------------------------------------------------------------------------

/** Run from the Apps Script editor to execute all booking tests. */
function runBookingTests() {
  Logger.log('=== BookingTest suite ===');

  _suite('createBooking — sets defaults correctly', function () {
    var fakeId  = 'test-id-001';
    var idGen   = function () { return fakeId; };
    var booking = createBooking({
      clientName:  'Олена',
      clientPhone: '+380501234567',
      bookingTime: '2025-12-31T19:00:00.000Z',
      guestCount:  4
    }, idGen);

    _assertEqual(booking.id,          fakeId,       'id');
    _assertEqual(booking.clientName,  'Олена',       'clientName');
    _assertEqual(booking.guestCount,  4,             'guestCount');
    _assertEqual(booking.status,      BookingStatus.PENDING, 'default status');
    _assertEqual(booking.reminderSent, false,        'reminderSent default');
    _assert(booking.bookingTime instanceof Date,     'bookingTime is Date');
  });

  _suite('createBooking — guestCount defaults to 1 when missing', function () {
    var b = createBooking({ clientName: 'Іван', clientPhone: '123', bookingTime: new Date() },
                          function () { return 'x'; });
    _assertEqual(b.guestCount, 1, 'guestCount');
  });

  _suite('createBooking — unknown status falls back to pending', function () {
    var b = createBooking({
      clientName:  'Марко',
      clientPhone: '123',
      bookingTime: new Date(),
      status:      'totally-invalid'
    }, function () { return 'x'; });
    _assertEqual(b.status, BookingStatus.PENDING, 'status');
  });

  _suite('createBooking — trims whitespace from string fields', function () {
    var b = createBooking({
      clientName:  '  Соня  ',
      clientPhone: ' 050 ',
      bookingTime: new Date()
    }, function () { return 'x'; });
    _assertEqual(b.clientName,  'Соня', 'clientName trimmed');
    _assertEqual(b.clientPhone, '050',  'clientPhone trimmed');
  });

  _suite('validateBooking — passes with minimum valid data', function () {
    var b = createBooking({
      clientName:  'Олег',
      clientPhone: '050',
      bookingTime: new Date()
    }, function () { return 'x'; });
    var r = validateBooking(b);
    _assert(r.valid, 'should be valid');
    _assertEqual(r.errors.length, 0, 'no errors');
  });

  _suite('validateBooking — fails when clientName is missing', function () {
    var b = createBooking({ clientPhone: '050', bookingTime: new Date() },
                          function () { return 'x'; });
    var r = validateBooking(b);
    _assert(!r.valid, 'should be invalid');
    _assert(r.errors.some(function (e) { return e.indexOf('clientName') !== -1; }),
            'error mentions clientName');
  });

  _suite('validateBooking — fails when both phone and telegramId are missing', function () {
    var b = createBooking({ clientName: 'X', bookingTime: new Date() },
                          function () { return 'x'; });
    var r = validateBooking(b);
    _assert(!r.valid, 'should be invalid');
    _assert(r.errors.some(function (e) { return e.indexOf('clientPhone') !== -1; }),
            'error mentions clientPhone or clientTelegramId');
  });

  _suite('validateBooking — fails when bookingTime is invalid', function () {
    var b = createBooking({ clientName: 'X', clientPhone: '1', bookingTime: 'not-a-date' },
                          function () { return 'x'; });
    var r = validateBooking(b);
    _assert(!r.valid, 'should be invalid');
    _assert(r.errors.some(function (e) { return e.indexOf('bookingTime') !== -1; }),
            'error mentions bookingTime');
  });

  _suite('validateBooking — passes when only telegramId is provided (no phone)', function () {
    var b = createBooking({
      clientName:       'Аня',
      clientTelegramId: '99999',
      bookingTime:      new Date()
    }, function () { return 'x'; });
    var r = validateBooking(b);
    _assert(r.valid, 'should be valid');
  });

  _suite('parseBookingFromRequest — returns error on non-object input', function () {
    var r = parseBookingFromRequest(null);
    _assert(!r.booking, 'booking should be null');
    _assert(r.errors.length > 0, 'errors should be non-empty');
  });

  _suite('parseBookingFromRequest — returns booking for valid input', function () {
    var r = parseBookingFromRequest({
      clientName:  'Катя',
      clientPhone: '050',
      bookingTime: new Date().toISOString()
    }, function () { return 'id1'; });
    _assert(r.booking !== null, 'booking should be set');
    _assertEqual(r.errors.length, 0, 'no errors');
  });

  _suite('bookingToRow / bookingFromRow — round-trip preserves data', function () {
    var original = createBooking({
      clientName:       'Тест',
      clientPhone:      '099',
      clientTelegramId: '12345',
      guestCount:       3,
      bookingTime:      new Date('2025-06-15T18:30:00.000Z'),
      tableNumber:      '5',
      notes:            'вікно',
      status:           BookingStatus.CONFIRMED,
      reminderSent:     true
    }, function () { return 'row-test-id'; });

    var row      = bookingToRow(original);
    var restored = bookingFromRow(row);

    _assertEqual(restored.id,               'row-test-id',        'id');
    _assertEqual(restored.clientName,       'Тест',               'clientName');
    _assertEqual(restored.guestCount,       3,                    'guestCount');
    _assertEqual(restored.status,           BookingStatus.CONFIRMED, 'status');
    _assertEqual(restored.reminderSent,     true,                 'reminderSent');
    _assertEqual(restored.tableNumber,      '5',                  'tableNumber');
    _assertEqual(restored.bookingTime.toISOString(),
                 original.bookingTime.toISOString(),              'bookingTime');
  });

  Logger.log('=== BookingTest done ===');
}
