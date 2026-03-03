/**
 * TelegramBot.gs — Telegram Bot API integration.
 *
 * Wraps UrlFetchApp calls so callers never have to deal with raw HTTP.
 * All functions return a plain { ok: boolean, error?: string } result so
 * callers can decide how to handle failures without throwing.
 */

/* global UrlFetchApp */

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Sends a plain-text or HTML message to a Telegram chat.
 *
 * @param {string} botToken  - Bot token from @BotFather.
 * @param {string} chatId    - Telegram chat/user ID.
 * @param {string} text      - Message text (HTML parse mode supported).
 * @returns {{ ok: boolean, error?: string }}
 */
function sendTelegramMessage(botToken, chatId, text) {
  if (!botToken || !chatId) {
    return { ok: false, error: 'botToken and chatId are required' };
  }

  var url     = 'https://api.telegram.org/bot' + botToken + '/sendMessage';
  var payload = JSON.stringify({
    chat_id:    chatId,
    text:       text,
    parse_mode: 'HTML'
  });

  try {
    var response = UrlFetchApp.fetch(url, {
      method:             'post',
      contentType:        'application/json',
      payload:            payload,
      muteHttpExceptions: true
    });

    var code = response.getResponseCode();
    if (code !== 200) {
      var body = response.getContentText();
      return { ok: false, error: 'HTTP ' + code + ': ' + body };
    }
    return { ok: true };
  } catch (e) {
    return { ok: false, error: String(e) };
  }
}

/**
 * Notifies all configured recipients (owner + staff) about a booking event.
 *
 * @param {string}   botToken
 * @param {string}   ownerChatId
 * @param {string[]} staffChatIds
 * @param {string}   text
 * @returns {{ ok: boolean, errors: string[] }}
 */
function notifyAll(botToken, ownerChatId, staffChatIds, text) {
  var recipients = [ownerChatId].concat(staffChatIds || []);
  var errors     = [];

  recipients.forEach(function (chatId) {
    if (!chatId) return;
    var result = sendTelegramMessage(botToken, chatId, text);
    if (!result.ok) {
      errors.push('chatId=' + chatId + ': ' + result.error);
    }
  });

  return { ok: errors.length === 0, errors: errors };
}

// ---------------------------------------------------------------------------
// Message builders
// ---------------------------------------------------------------------------

/**
 * Builds a human-readable notification message for a new booking.
 *
 * @param {Booking} booking
 * @returns {string}
 */
function buildNewBookingMessage(booking) {
  return [
    '🆕 <b>Нове замовлення</b>',
    '👤 ' + _esc(booking.clientName),
    '📞 ' + (_esc(booking.clientPhone) || '—'),
    '👥 Гостей: ' + booking.guestCount,
    '📅 Час: '    + _fmtDate(booking.bookingTime),
    booking.tableNumber ? ('🪑 Стіл: ' + _esc(booking.tableNumber)) : '',
    booking.notes       ? ('📝 ' + _esc(booking.notes))             : '',
    '🔑 ID: <code>' + _esc(booking.id) + '</code>'
  ].filter(Boolean).join('\n');
}

/**
 * Builds a reminder message to send to the client.
 *
 * @param {Booking} booking
 * @returns {string}
 */
function buildReminderMessage(booking) {
  return [
    '⏰ <b>Нагадування про бронювання</b>',
    'Привіт, ' + _esc(booking.clientName) + '!',
    'Ваш столик заброньований на ' + _fmtDate(booking.bookingTime) + '.',
    'Кількість гостей: ' + booking.guestCount + '.',
    booking.tableNumber ? ('Стіл: ' + _esc(booking.tableNumber) + '.') : '',
    '',
    'Будемо раді вас бачити! Якщо плани змінились — напишіть нам.'
  ].filter(Boolean).join('\n');
}

/**
 * Builds a status-change notification for staff.
 *
 * @param {Booking} booking
 * @param {string}  oldStatus
 * @returns {string}
 */
function buildStatusChangeMessage(booking, oldStatus) {
  var statusLabel = {
    pending:   '⏳ Очікує',
    confirmed: '✅ Підтверджено',
    cancelled: '❌ Скасовано',
    completed: '🏁 Завершено'
  };
  return [
    '🔄 <b>Статус змінено</b>',
    '👤 ' + _esc(booking.clientName),
    '📅 ' + _fmtDate(booking.bookingTime),
    (statusLabel[oldStatus] || oldStatus) + ' → ' + (statusLabel[booking.status] || booking.status),
    '🔑 ID: <code>' + _esc(booking.id) + '</code>'
  ].join('\n');
}

// ---------------------------------------------------------------------------
// Private helpers
// ---------------------------------------------------------------------------

/** Escapes HTML special characters for Telegram HTML parse mode. */
function _esc(str) {
  return String(str || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

/** Formats a Date for display (Ukrainian locale approximation). */
function _fmtDate(date) {
  if (!(date instanceof Date) || isNaN(date.getTime())) return '—';
  var pad = function (n) { return n < 10 ? '0' + n : String(n); };
  return pad(date.getDate()) + '.' + pad(date.getMonth() + 1) + '.' + date.getFullYear()
    + ' ' + pad(date.getHours()) + ':' + pad(date.getMinutes());
}
