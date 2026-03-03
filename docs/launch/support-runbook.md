# Support Runbook

## Issue: No response in Instagram DM

Check:

1. MacroDroid macro is enabled.
2. Tablet is online and app is not battery-killed.
3. HTTP request action still has valid Apps Script URL.

## Issue: No row in Google Sheet

Check:

1. Apps Script deployment is active.
2. Script access is set to `Anyone`.
3. Spreadsheet name matches `CONFIG.sheetName`.
4. Macro sends JSON payload (not empty body).

## Issue: POS does not show new menu

Check:

1. JSON file is valid UTF-8 and valid JSON.
2. POS import flow completed without interruption.
3. Correct menu file version was used.

## Issue: Tablet sleeps and automation stops

Check:

1. Battery optimization disabled for MacroDroid.
2. Screen timeout increased.
3. MacroDroid pinned in recent apps.

## Escalation

If issue persists, collect:

- Screenshot of the failing step
- Current release version
- Approximate timestamp
- Error message text

Then escalate to technical owner with this context.
