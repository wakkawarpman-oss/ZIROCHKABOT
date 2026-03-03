/**
 * Config.gs — Runtime configuration for ZIROCHKABOT.
 *
 * Fill in these values via Script Properties (File → Project properties →
 * Script properties) so that secrets are never stored in source code.
 * Keys used:
 *   SPREADSHEET_ID, SHEET_NAME, TELEGRAM_BOT_TOKEN,
 *   OWNER_CHAT_ID, STAFF_CHAT_IDS (comma-separated),
 *   REMINDER_MINUTES_BEFORE, TIMEZONE
 */

/* global PropertiesService */

/**
 * Returns the merged runtime configuration.
 * Script Properties override the defaults below.
 *
 * @returns {{
 *   spreadsheetId: string,
 *   sheetName: string,
 *   telegramBotToken: string,
 *   ownerChatId: string,
 *   staffChatIds: string[],
 *   reminderMinutesBefore: number,
 *   timezone: string
 * }}
 */
function getConfig() {
  var props = PropertiesService.getScriptProperties().getProperties();

  var staffRaw = props['STAFF_CHAT_IDS'] || '';
  var staffChatIds = staffRaw
    ? staffRaw.split(',').map(function (s) { return s.trim(); }).filter(Boolean)
    : [];

  return {
    spreadsheetId:       props['SPREADSHEET_ID']          || '',
    sheetName:           props['SHEET_NAME']               || 'Bookings',
    telegramBotToken:    props['TELEGRAM_BOT_TOKEN']       || '',
    ownerChatId:         props['OWNER_CHAT_ID']            || '',
    staffChatIds:        staffChatIds,
    reminderMinutesBefore: parseInt(props['REMINDER_MINUTES_BEFORE'], 10) || 60,
    timezone:            props['TIMEZONE']                 || 'Europe/Kyiv'
  };
}
