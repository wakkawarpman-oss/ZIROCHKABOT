/**
 * tests/ReminderTest.gs — Unit tests for reminder timing logic in Booking.gs.
 *
 * Run from the Apps Script editor: open this file and run `runReminderTests()`.
 * A summary is printed to Logger (View → Logs).
 *
 * No external services are called.
 */

/* global Logger */

// ---------------------------------------------------------------------------
// Helpers (re-uses _suite / _assert / _assertEqual from BookingTest.gs)
// If running this file standalone, define a local version of the runner.
// ---------------------------------------------------------------------------

function _reminderSuite(name, fn) {
  if (typeof _suite === 'function') {
    _suite(name, fn);
  } else {
    try {
      fn();
      Logger.log('✅ PASS  ' + name);
    } catch (e) {
      Logger.log('❌ FAIL  ' + name + '\n       ' + e.message);
    }
  }
}

function _ra(condition, msg) {
  if (!condition) throw new Error('Assertion failed: ' + (msg || ''));
}

function _re(actual, expected, msg) {
  if (actual !== expected) {
    throw new Error((msg ? msg + ': ' : '') +
      'expected ' + JSON.stringify(expected) + ' but got ' + JSON.stringify(actual));
  }
}

// ---------------------------------------------------------------------------
// Test entry point
// ---------------------------------------------------------------------------

/** Run from the Apps Script editor to execute all reminder tests. */
function runReminderTests() {
  Logger.log('=== ReminderTest suite ===');

  var idGen = function () { return 'r-test'; };

  // ---- minutesUntilBooking ------------------------------------------------

  _reminderSuite('minutesUntilBooking — returns positive value for future booking', function () {
    var now     = new Date('2025-06-15T10:00:00.000Z');
    var booking = createBooking({
      clientName:  'A', clientPhone: '1',
      bookingTime: new Date('2025-06-15T11:00:00.000Z')
    }, idGen);
    var mins = minutesUntilBooking(booking, now);
    _re(mins, 60, 'minutes until booking');
  });

  _reminderSuite('minutesUntilBooking — returns negative for past booking', function () {
    var now     = new Date('2025-06-15T12:00:00.000Z');
    var booking = createBooking({
      clientName:  'A', clientPhone: '1',
      bookingTime: new Date('2025-06-15T11:00:00.000Z')
    }, idGen);
    var mins = minutesUntilBooking(booking, now);
    _ra(mins < 0, 'should be negative, got ' + mins);
  });

  _reminderSuite('minutesUntilBooking — returns 0 when booking is right now', function () {
    var now     = new Date('2025-06-15T11:00:00.000Z');
    var booking = createBooking({
      clientName:  'A', clientPhone: '1',
      bookingTime: new Date('2025-06-15T11:00:00.000Z')
    }, idGen);
    _re(minutesUntilBooking(booking, now), 0, 'minutes');
  });

  _reminderSuite('minutesUntilBooking — uses current time when now is omitted', function () {
    // Booking far in the future — should be positive
    var booking = createBooking({
      clientName:  'A', clientPhone: '1',
      bookingTime: new Date(Date.now() + 3600 * 1000)
    }, idGen);
    _ra(minutesUntilBooking(booking) > 0, 'should be positive');
  });

  // ---- shouldSendReminder -------------------------------------------------

  _reminderSuite('shouldSendReminder — true when confirmed, not sent, within window', function () {
    var now     = new Date('2025-06-15T10:10:00.000Z');
    var booking = createBooking({
      clientName:   'B', clientPhone: '1',
      bookingTime:  new Date('2025-06-15T11:00:00.000Z'),
      status:       'confirmed',
      reminderSent: false
    }, idGen);
    _ra(shouldSendReminder(booking, 60, now), 'should send reminder');
  });

  _reminderSuite('shouldSendReminder — false when already sent', function () {
    var now     = new Date('2025-06-15T10:10:00.000Z');
    var booking = createBooking({
      clientName:   'B', clientPhone: '1',
      bookingTime:  new Date('2025-06-15T11:00:00.000Z'),
      status:       'confirmed',
      reminderSent: true      // already sent
    }, idGen);
    _ra(!shouldSendReminder(booking, 60, now), 'should NOT send');
  });

  _reminderSuite('shouldSendReminder — false when status is pending (not confirmed)', function () {
    var now     = new Date('2025-06-15T10:10:00.000Z');
    var booking = createBooking({
      clientName:   'B', clientPhone: '1',
      bookingTime:  new Date('2025-06-15T11:00:00.000Z'),
      status:       'pending'
    }, idGen);
    _ra(!shouldSendReminder(booking, 60, now), 'should NOT send');
  });

  _reminderSuite('shouldSendReminder — false when booking is in the past', function () {
    var now     = new Date('2025-06-15T12:00:00.000Z');
    var booking = createBooking({
      clientName:   'B', clientPhone: '1',
      bookingTime:  new Date('2025-06-15T11:00:00.000Z'), // past
      status:       'confirmed',
      reminderSent: false
    }, idGen);
    _ra(!shouldSendReminder(booking, 60, now), 'should NOT send for past booking');
  });

  _reminderSuite('shouldSendReminder — false when too far in the future', function () {
    var now     = new Date('2025-06-15T08:00:00.000Z');
    var booking = createBooking({
      clientName:   'B', clientPhone: '1',
      bookingTime:  new Date('2025-06-15T11:00:00.000Z'), // 180 min away
      status:       'confirmed',
      reminderSent: false
    }, idGen);
    _ra(!shouldSendReminder(booking, 60, now), 'should NOT send 3 h early');
  });

  _reminderSuite('shouldSendReminder — true exactly at the reminder window boundary', function () {
    var now     = new Date('2025-06-15T10:00:00.000Z');
    var booking = createBooking({
      clientName:   'B', clientPhone: '1',
      bookingTime:  new Date('2025-06-15T11:00:00.000Z'), // exactly 60 min away
      status:       'confirmed',
      reminderSent: false
    }, idGen);
    _ra(shouldSendReminder(booking, 60, now), 'should send at boundary');
  });

  _reminderSuite('shouldSendReminder — true when booking time is now (0 minutes)', function () {
    var now     = new Date('2025-06-15T11:00:00.000Z');
    var booking = createBooking({
      clientName:   'B', clientPhone: '1',
      bookingTime:  new Date('2025-06-15T11:00:00.000Z'),
      status:       'confirmed',
      reminderSent: false
    }, idGen);
    _ra(shouldSendReminder(booking, 60, now), 'should send at booking time');
  });

  _reminderSuite('shouldSendReminder — false when status is cancelled', function () {
    var now     = new Date('2025-06-15T10:10:00.000Z');
    var booking = createBooking({
      clientName:   'C', clientPhone: '1',
      bookingTime:  new Date('2025-06-15T11:00:00.000Z'),
      status:       'cancelled',
      reminderSent: false
    }, idGen);
    _ra(!shouldSendReminder(booking, 60, now), 'should NOT send for cancelled');
  });

  Logger.log('=== ReminderTest done ===');
}

// ---------------------------------------------------------------------------
// Convenience: run all test suites at once
// ---------------------------------------------------------------------------

/** Run all test suites in sequence. */
function runAllTests() {
  runBookingTests();
  runReminderTests();
}
