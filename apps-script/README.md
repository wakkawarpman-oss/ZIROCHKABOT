# Apps Script Integration

`Code.gs` is a webhook endpoint for saving booking requests into Google Sheets.

## Setup

1. Open `https://sheets.new`.
2. Name the spreadsheet `Bronyuvannya Zirochka`.
3. Open `Extensions -> Apps Script`.
4. Replace default code with contents of `Code.gs`.
5. Click `Deploy -> New deployment -> Web app`.
6. Set:
   - `Execute as`: Me
   - `Who has access`: Anyone
7. Copy the generated Web App URL.

## MacroDroid Mapping

In your macro action `HTTP request`, send POST JSON to the Web App URL.

Example payload:

```json
{
  "source": "instagram_dm",
  "instagram_username": "guest_account",
  "guest_name": "Anna",
  "phone": "+380501112233",
  "guests": "4",
  "visit_date": "2026-03-05",
  "visit_time": "19:00",
  "message": "Stolyk na vechir, bud laska"
}
```

## Result in Google Sheets

Rows are saved with:

- timestamp
- source and username
- booking details
- raw payload for debugging

## Notes

- If you want to force one specific spreadsheet, set `CONFIG.spreadsheetId`.
- Keep credentials outside git and do not hardcode private tokens in script code.
