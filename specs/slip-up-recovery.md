# Slip-up recovery

- Date: 2026-09-28
- Owner: Madhu Kinnera
- Status: approved

## Problem
When a Heal user breaks no contact, "reset" sets the streak start to now and every past day is gone.
The reset sheet says "your days still count", but they don't. Reset is the moment users churn.

## Solution
A slip still starts a new streak, but the old one is logged as a slip: date, days it reached, optional reason.
The app derives "total clean days" (sum of slips + current streak) and "best streak" from that log.

## Screens / UX (app)
- Slip sheet: optional reason chips (I texted them / I replied to them / We called or met / Something else), button "Start a new streak".
- Streak card: "N total clean days · best streak M", shown only after the first slip.

## Data / API
`POST /api/v1/heal/sync` and `GET /api/v1/heal/profile` gain one field:
```json
"slips": [{ "date": "2026-09-20T10:00:00.000", "streakDays": 12, "reason": "I texted them" }]
```
- Stored on `no_contact_profiles.slips` (MongoDB, no migration).
- `slips` absent/null in a sync request → stored slips are kept (older app versions don't send it).
- `reason` is optional.

## Edge cases
- Offline slip: saved locally, but the app's existing cloud-first restore on launch overwrites local data with the cloud copy, so a slip made offline can be lost (same as check-ins and burned messages today). Fixing that sync order is a separate change.
- Old documents without `slips` → returned as an empty list.

## Out of scope
- Slip analytics / dashboards, editing or deleting slips, slip-based notifications.

## Acceptance checklist
- [x] Success path defined
- [x] Error states defined (missing field keeps stored data)
- [x] Dependencies: app release carrying the new field
