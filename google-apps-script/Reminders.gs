/**
 * Reminders.gs — Reminder scheduling and dispatch logic.
 *
 * Designed to be called from a time-based Apps Script trigger every 5–15 min.
 * All time-sensitive decisions are made via the pure helpers in Booking.gs so
 * the logic can be unit-tested without external dependencies.
 */

/* global Logger */

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Main reminder job — finds all bookings that are due for a reminder,
 * sends the reminder via Telegram and marks them as sent in the sheet.
 *
 * Call this from a time-based trigger (every 5 or 10 minutes).
 *
 * @param {{
 *   spreadsheetId: string,
 *   sheetName: string,
 *   telegramBotToken: string,
 *   ownerChatId: string,
 *   reminderMinutesBefore: number
 * }} config
 * @param {Date} [now] - Optional reference time for testing.
 * @returns {{ sent: number, failed: number }}
 */
function processReminders(config, now) {
  var pending = getPendingReminderBookings(
    config.spreadsheetId,
    config.sheetName,
    config.reminderMinutesBefore,
    now
  );

  var sent   = 0;
  var failed = 0;

  pending.forEach(function (booking) {
    var result = _dispatchReminder(config, booking);
    if (result.ok) {
      // Mark as sent so we don't re-send
      booking.reminderSent = true;
      updateBooking(config.spreadsheetId, config.sheetName, booking);
      sent++;
      Logger.log('Reminder sent for booking ' + booking.id);
    } else {
      failed++;
      Logger.log('Failed to send reminder for booking ' + booking.id + ': ' + result.error);
    }
  });

  return { sent: sent, failed: failed };
}

// ---------------------------------------------------------------------------
// Private helpers
// ---------------------------------------------------------------------------

/**
 * Sends the reminder Telegram message to the client (if they have a Telegram
 * ID) and/or to the owner as a heads-up.
 *
 * @param {Object}  config
 * @param {Booking} booking
 * @returns {{ ok: boolean, error?: string }}
 */
function _dispatchReminder(config, booking) {
  var text = buildReminderMessage(booking);

  // Notify client if we have their Telegram ID
  if (booking.clientTelegramId) {
    var clientResult = sendTelegramMessage(
      config.telegramBotToken,
      booking.clientTelegramId,
      text
    );
    if (!clientResult.ok) {
      return clientResult; // surface error to caller
    }
  }

  // Always notify owner as a reference
  if (config.ownerChatId) {
    var ownerText   = '⏰ <b>Нагадування надіслано</b> клієнту ' +
                      booking.clientName + ' (ID: <code>' + booking.id + '</code>)';
    sendTelegramMessage(config.telegramBotToken, config.ownerChatId, ownerText);
  }

  return { ok: true };
}
