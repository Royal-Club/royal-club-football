# Firebase Push (FCM) — Setup & Management Guide

This guide sets up **free** push notifications for the tournament RSVP reminders.

- **Backend** (`royal-club-football`) sends push via the Firebase Admin SDK — needs a **service-account JSON**.
- **Mobile app** (Expo, Phase 2) receives push — needs the **Firebase app config** files.

> 💰 **Cost:** FCM push is **free and unlimited** on Firebase's **Spark (free) plan**. No credit card
> required for push. The only unavoidable costs are the **Apple Developer account ($99/yr)** and
> **Google Play ($25 one-time)** — those are app-store fees, not Firebase, and only needed to publish
> the app / enable iOS push.

---

## Part 0 — What you'll end up with

| Artifact | Used by | Where it goes | Secret? |
|---|---|---|---|
| Service-account JSON | Backend | `secrets/firebase-service-account.json` (git-ignored) | 🔒 **YES — never commit** |
| `google-services.json` | Expo Android app | app project root | Not sensitive (safe to commit) |
| `GoogleService-Info.plist` | Expo iOS app | app project root | Not sensitive (safe to commit) |
| APNs Auth Key (`.p8`) | Firebase (for iOS) | uploaded into Firebase console | 🔒 keep the file safe |

---

## Part 1 — Create the Firebase project (free)

1. Go to **https://console.firebase.google.com** and sign in with a Google account.
   - Use a **club-owned account** (e.g. a shared `royalclub@gmail.com`), not a personal one, so
     access survives people leaving. See *Part 5 — Managing it*.
2. Click **Add project** → name it e.g. `royal-club-football` → Continue.
3. Google Analytics: **you can disable it** (not needed for push) → Create project.
4. When it finishes, you're on the **Spark (free) plan** by default. ✅ Leave it there — do **not**
   upgrade to Blaze. FCM works fully on Spark.

---

## Part 2 — Backend: get the service-account JSON

This is the credential the Spring Boot app uses to send push.

1. In the Firebase console: **⚙️ (gear icon) → Project settings**.
2. Open the **Service accounts** tab.
3. Click **Generate new private key** → **Generate key**. A JSON file downloads (e.g.
   `royal-club-football-firebase-adminsdk-xxxxx.json`).
   - 🔒 **Treat this like a password.** Anyone with it can send push as your app. Never commit it,
     never paste it in chat/email/Slack.
4. Put it in a **git-ignored** folder in the backend repo:

   ```
   royal-club-football/
   └── secrets/
       └── firebase-service-account.json      ← the file you downloaded, renamed
   ```

   (`secrets/` and `*firebase*.json` are already in `.gitignore`.)

5. Point the app at it. Two options — pick one:

   **Option A — file path (simplest for local dev):**
   ```bash
   # Windows PowerShell
   $env:FIREBASE_CREDENTIALS_PATH = "D:\royal-club-football\secrets\firebase-service-account.json"

   # Linux/macOS
   export FIREBASE_CREDENTIALS_PATH="/path/to/secrets/firebase-service-account.json"
   ```

   **Option B — raw JSON in an env var (best for servers / CI / Docker):**
   ```bash
   export FIREBASE_CREDENTIALS_JSON='{"type":"service_account","project_id":"...", ... }'
   ```

   Both are already wired in `application.yml`:
   ```yaml
   firebase:
     credentials-path: ${FIREBASE_CREDENTIALS_PATH:}
     credentials-json: ${FIREBASE_CREDENTIALS_JSON:}
   ```

6. Restart the backend. On boot you should see:
   ```
   Firebase initialized; push notifications are ENABLED.
   ```
   (If neither var is set you'll instead see a warning that push is DISABLED — the app still runs,
   sends just become no-ops.)

> ✅ **The backend is fully usable at this point** — you can test the whole reminder pipeline without
> touching the mobile app yet (see *Part 4*), because push targets any registered device token.

---

## Part 2.5 — Production / server deployment (important)

The `application.yml` default (`./secret/…json`) exists only on your dev machine. **On the production
server that file is absent, so push is DISABLED by default** — the app runs fine, but reminders are
logged no-ops and never delivered. To turn push on in production you must give the server the
credential via an environment variable (do **not** copy the JSON file onto the box or into the image):

**Option A — raw JSON in an env var (recommended):**
```bash
# value = the entire service-account JSON on one line
export FIREBASE_CREDENTIALS_JSON='{"type":"service_account","project_id":"royalclub-1ff75", ... }'
```
- **Docker:** `docker run -e FIREBASE_CREDENTIALS_JSON="$(cat secret/royalclub-*.json)" ...`
  (or put it in a Docker/compose secret, not the image).
- **Jenkins:** store the JSON as a *Secret text* credential and inject it as `FIREBASE_CREDENTIALS_JSON`
  in the deploy stage.
- **systemd / plain server:** add `Environment=FIREBASE_CREDENTIALS_JSON=...` (or an `EnvironmentFile`).

**Option B — a file on the server:** place the JSON somewhere readable and set
`FIREBASE_CREDENTIALS_PATH=/etc/royalclub/firebase.json`.

**Confirm it worked** — check the startup log for exactly one of these lines:
```
Firebase initialized; push notifications are ENABLED.     ← good, push will send
... push notifications are DISABLED (no-op).              ← credential not picked up
```
Also set the reminder cadence/window per environment if needed via `REMINDERS_CRON`,
`REMINDERS_WINDOW_HOURS`, `REMINDERS_MAX_PER_PLAYER` (see `application.yml`).

> 🔒 Never commit the JSON or bake it into the Docker image. Env var / secret store only.

---

## Part 3 — Mobile app config (Phase 2 — Expo)

You'll need these when we build the Expo app. Register one app per platform in the **same** Firebase
project (**Project settings → General → Your apps**).

### Android
1. **Add app → Android.**
2. **Package name:** must match the app's `applicationId` (e.g. `com.royalclub.football`). Pick it now
   and keep it consistent.
3. Download **`google-services.json`** → place at the Expo project root (safe to commit).

### iOS (needs an Apple Developer account)
1. **Add app → iOS.**
2. **Bundle ID:** e.g. `com.royalclub.football` (keep consistent with Android naming scheme).
3. Download **`GoogleService-Info.plist`** → place at the Expo project root.
4. **Enable APNs (Apple Push Notification service)** — required for iOS push:
   - In the **Apple Developer** portal → **Certificates, Identifiers & Profiles → Keys** → create a
     new key with **Apple Push Notifications service (APNs)** enabled → download the **`.p8`** file
     (you also get a **Key ID**; note your **Team ID**).
   - In **Firebase → Project settings → Cloud Messaging → Apple app configuration → APNs Authentication
     Key** → **Upload** the `.p8`, and enter the **Key ID** + **Team ID**.
   - 🔒 Apple lets you download the `.p8` **only once** — store it safely.

> Android push works without any of the Apple steps. If you only ship Android first, you can skip the
> entire iOS section until later.

---

## Part 4 — End-to-end test (no mobile app required)

You can prove the whole backend flow works using any FCM token (even a throwaway one).

1. Start MySQL (`docker-compose up -d`) and the backend with `FIREBASE_CREDENTIALS_PATH` set.
2. **Log in** to get a JWT: `POST /auth/login`.
3. **Register a device token:**
   ```
   POST /device-tokens
   Authorization: Bearer <jwt>
   { "token": "<an FCM device token>", "platform": "ANDROID" }
   ```
4. **Create an upcoming tournament** dated ~1 day out, and leave your player without an RSVP row.
5. **See who's pending:** `GET /tournament-participants/{tournamentId}/pending` → your player is listed.
6. **Trigger a reminder now:** `POST /tournament-participants/{tournamentId}/remind`
   - With Firebase configured → the device receives the push.
   - Response shows `remindedCount`. A row is written to `tournament_reminder_log`.
7. **Answer Yes/No:** `POST /tournament-participants` → the player disappears from `/pending` and is no
   longer reminded.
8. **Cap check:** hit `/remind` more than `reminders.max-per-player` (default 3) times → further sends
   for that player are skipped.

The scheduled job (`reminders.cron`, default 9:00 & 18:00 Asia/Dhaka) does step 6 automatically for
every tournament within `reminders.window-hours` (default 48h) of kickoff.

---

## Part 5 — Managing it (ongoing)

**Who owns access**
- Create the project under a **shared club Google account**, then add teammates as **Members** under
  **Project settings → Users and permissions** (role: *Editor* for devs, *Viewer* otherwise). This way
  no single person's departure locks you out.

**Rotating / revoking the backend key** (do this if a key leaks or someone leaves)
- **Project settings → Service accounts → Manage service account permissions** (opens Google Cloud) →
  **Keys** → delete the old key, **Add key → Create new key (JSON)**, then update
  `FIREBASE_CREDENTIALS_PATH`/`_JSON` and restart. Old key stops working immediately.

**Where secrets live**
- Local dev: `secrets/` folder (git-ignored). Never commit.
- Server/CI: set `FIREBASE_CREDENTIALS_JSON` as a protected environment variable / secret store — do
  not bake the file into the Docker image.

**Config knobs** (all in `application.yml`, override via env vars):
| Setting | Env var | Default | Meaning |
|---|---|---|---|
| `reminders.window-hours` | `REMINDERS_WINDOW_HOURS` | 48 | Start reminding this many hours before kickoff |
| `reminders.max-per-player` | `REMINDERS_MAX_PER_PLAYER` | 3 | Stop after N reminders per player per tournament |
| `reminders.cron` | `REMINDERS_CRON` | `0 0 9,18 * * ?` | When the reminder job runs (Asia/Dhaka) |

**Monitoring**
- Backend logs show each run: `RSVP reminder job finished; dispatched N reminder(s).`
- Dead/uninstalled devices are auto-cleaned: the app deletes tokens FCM reports as `UNREGISTERED` /
  `INVALID_ARGUMENT`, so the token table stays healthy on its own.

**Staying free**
- Keep the project on **Spark**. Only FCM is used, which is free. Do not enable Blaze-billed products
  (Cloud Functions, Firestore, Hosting) unless you deliberately add them later.

---

## Quick checklist

- [ ] Firebase project created (Spark/free)
- [ ] Service-account JSON downloaded → `secrets/firebase-service-account.json`
- [ ] `FIREBASE_CREDENTIALS_PATH` (or `_JSON`) set; boot log says push **ENABLED**
- [ ] `POST /device-tokens` + `POST /{id}/remind` test push received
- [ ] (Phase 2) Android `google-services.json` obtained
- [ ] (Phase 2, iOS) `GoogleService-Info.plist` + APNs `.p8` uploaded to Firebase
- [ ] Team members added; key-rotation process understood
