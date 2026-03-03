# Releases Guide

This file documents how to prepare and publish release artifacts for restaurant launch.

## Expected Files

For each release tag, attach:

- `ZirochkaPOS.apk`
- `zirochka_instagram_bot.mdr`
- `menu_zirochka.json`
- `STAFF_GUIDE.pdf` (optional)

## Naming Convention

Use semantic versioning:

- Tag: `v1.0.0`
- APK: `ZirochkaPOS-v1.0.0.apk`
- Macro: `zirochka_instagram_bot-v1.0.0.mdr`
- Menu: `menu_zirochka-v1.0.0.json`

## Release Notes Template

```
## ZIROCHKABOT vX.Y.Z

### Included
- POS app update:
- Macro update:
- Menu update:

### Launch checks
- [ ] Test booking message in Instagram
- [ ] New row appears in Google Sheets
- [ ] POS accepts order with current menu
- [ ] Tablet power settings verified
```

## Publishing Steps

1. Create git tag for release.
2. Draft a GitHub release.
3. Upload all required assets.
4. Copy release link to staff onboarding materials.
