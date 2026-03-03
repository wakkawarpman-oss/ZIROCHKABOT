# ZIROCHKABOT

Full project launch kit for the Zirochka restaurant booking bot and tablet POS onboarding.

## Overview

This repository contains everything needed to launch and train staff on the ZIROCHKABOT flow:

- Instagram-to-Google-Sheets booking bridge (Apps Script)
- POS setup package structure (APK + JSON menu + macro placeholders)
- Canva video training kit (full and short versions)
- Go-live checklist and release process notes

## Quick Start (Go Live in 30-60 minutes)

1. Download or prepare release files listed in `releases/RELEASES.md`.
2. Deploy Google Apps Script from `apps-script/Code.gs`.
3. Set your tablet macro HTTP request to your deployed Web App URL.
4. Import POS menu from `config/menu_zirochka.sample.json` (or your production menu file).
5. Run the checks in `docs/launch/go-live-checklist.md`.
6. Share training materials from `canva-video-kit/`.

## Repository Structure

- `apps-script/` - Google Apps Script webhook to capture booking events in Sheets
- `config/` - sample configuration files (menu structure)
- `releases/` - release assets checklist and publishing notes
- `docs/launch/` - operational launch runbook and go-live checklist
- `canva-video-kit/` - video production package for staff training in Canva

## Release Artifacts

Expected artifacts for each launch version:

- `ZirochkaPOS.apk` (Android POS app)
- `zirochka_instagram_bot.mdr` (MacroDroid macro export)
- `menu_zirochka.json` (POS menu data)
- Optional: `STAFF_GUIDE.pdf` and release notes

Detailed structure and naming: `releases/RELEASES.md`.

## Canva Training Materials

Inside `canva-video-kit/` you will find:

- Full version (~16:30): 15 scenes
- Short version (~5:50): 7 scenes
- Bulk Create CSV files, timing plans, and UA voiceover scripts
- Two reference MP4 previews

## Security Notes

- Do not commit production credentials or private API keys.
- Keep production Google Sheet IDs in script properties (not hardcoded).
- If your endpoint becomes public, add a lightweight shared token check.

## License

MIT (see `LICENSE`).
