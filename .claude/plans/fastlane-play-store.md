# Upload releases to Google Play with fastlane

**Status: decisions taken on 2026-11-24. Step 1 waits for the service account's access, steps 5 and 6 for your list of screens.** Independent of every other plan.

## Context

You want fastlane to upload the app, the release notes, the screenshots and the texts, as in `../Cephalopod-Flutter`. This also closes the two TODOs in `RELEASING.md`.

### What Cephalopod does

- Shell scripts call `fastlane supply`. There is no Fastfile, Appfile or Gemfile.
- `fastlane/metadata/android/<locale>/` holds the texts and `changelogs/<versionCode>.txt`.
- `art/play-store/<language>/` holds the screenshots. A script stages them next to the texts in a temporary folder.
- Each script has `--stage-only` (no key needed), `--validate-only` (Play checks, nothing changes), `--track` and `--version-code`.
- The service account key stays outside the repository, in `PLAY_JSON_KEY`.

### Cephalopod's screenshot rules, followed here

From its `art/README.md`, `tool/take_store_screenshots.sh` and test:

- A test runs the real app on a device, so the text is drawn in the real font.
- It tells only the app to use each language; the device's own language stays as it is.
- It starts from cleared app data and goes through the same steps in every language.
- The pictures are 1080 x 1920. The script sets the screen to that size for the run and resets it at the end.
- They are saved as `art/play-store/<language>/<n>-<screen>.png`, numbered in the order the store shows them. Cephalopod's last one is an earlier screen in the dark theme.
- One command takes every language, or only those named. `art/README.md` lists the pictures and the command.
- They are taken again after a change to how the screens look.

**Yours (changed on 2026-11-25):** they show the release app (a `screenshots` build of it) and are taken after the version commit. So the script stops unless the working tree is clean (other than `MEMORY.md`) and the last commit is `Version <code> (<name>) final`.

**Different, with the reason:**
- Emulator only. Cephalopod also allows a phone, but clearing the app's data on your Pixel would delete your saved distances (debug and release share the package).
- PNG without alpha. Play's help page asks for "JPEG or 24-bit PNG (no alpha)". Cephalopod's PNGs do have an alpha channel.
- Three devices, at your request: phone, 7" tablet and 10" tablet. Cephalopod only takes phone pictures, so ours get a folder per device: `art/play-store/<language>/<device>/<n>-<screen>.png`.
- A clean status bar. Flutter's captures have no status bar, ours do, and Play's help page asks for no notifications or carrier and for full battery and signal icons.

### What else is different here

| | Cephalopod | Distance From Me |
|---|---|---|
| App on Play | new, a draft | live, so its listing is downloaded first and never rewritten blind |
| Upload of the app | by hand | by a script, to the internal track |
| Notes | a second upload after the bundle | in the same upload as the bundle, so both go up or neither does |
| Version | `app/pubspec.yaml` | `gradle/libs.versions.toml` |
| Screenshots | no network | Google Map, so they need network, an API key and a loaded map |
| Languages | 12 | 7 |
| Devices in the screenshots | phone | phone, 7" tablet, 10" tablet |

### Facts the scripts depend on

- **Listing uploads need a release:** `supply` looks for the version on the track even when it uploads no bundle (Cephalopod bc3b932). Every script passes `--track` and `--version_code`.
- **Missing notes become empty notes:** if a language has no `changelogs/<code>.txt`, `supply` sends an empty text. The script must stop first.
- **Stray APKs:** `supply` picks up any `*.apk` or `*.aab` in the current folder, so every call passes explicit `--skip_upload_*` flags.
- **The listing isn't part of a release:** new texts and screenshots go to review for all users as soon as they are uploaded. They go up once you have promoted to production.
- **`MEMORY.md`:** the memory hook keeps it modified, so the clean tree check needs an exception for it.
- **Ruby:** fastlane 2.239.0 is installed and works, but warns that it will soon need Ruby 3.3 (this machine has 3.2.3). Don't update fastlane without updating Ruby.
- **Lint:** not run while planning. It is a release gate, so step 7 runs it.

## Decisions

| | Decision |
|---|---|
| Scripts | As in Cephalopod, no Fastfile, in a new `scripts/` folder (`tools/` already holds Gradle modules) |
| Format | AAB from now on (4.0.3 was an APK) |
| Track | The script uploads to internal testing only. You promote to production in Play Console, once your testers have tested it |
| Who uploads | Claude runs `--validate-only`. **You run every upload**, and Claude gives you the exact command each time |
| Play notes | An unordered list, separate from `whats_new_message`, which stays a short paragraph |
| Screenshots | Cephalopod's rules, of the release build after the version commit, for phones, 7" tablets and 10" tablets. Git ignores the pictures |

## How each step is worked

- One step at a time, one commit per step, and only when you ask for the commit.
- Each step starts with an explanation and ends with its checks and the diff.
- **An upload can't start by accident:** without `--validate-only` or `--stage-only`, a script asks you to type the version name, and stops if it isn't run in a terminal. So you run it in your own terminal, not through Claude Code.

## Group 0: access

### 0. Play access (you, no commit)
- **Service account (open):** you think Cephalopod's only sees Cephalopod. Give the same one access to this app: Play Console, Users and permissions, the service account's email, App permissions, Add app, Distance From Me. It needs **Manage store presence** and **Release apps to testing tracks**, and not production. The key file stays the same.
- **Key:** outside the repository, with `PLAY_JSON_KEY` exported in your shell profile so that Claude's `--validate-only` sees it. Claude uses only the path. Check it with:
  ```bash
  fastlane run validate_play_store_json_key json_key:"$PLAY_JSON_KEY"
  ```
- **Play App Signing (done):** the app is enrolled (2026-11-24), so an AAB can be uploaded. Before step 3, compare the upload key certificate's SHA-256 in Play Console with the one that signs our release builds:
  ```bash
  ~/bin/android-sdk/build-tools/36.0.0/apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
  ```

## Group 1: texts and uploads

### 1. Bring the live listing into the repository
- **Read only:** download into the scratchpad. `supply init` opens an edit and aborts it.
  ```bash
  fastlane supply init --package_name gc.david.dfm --json_key "$PLAY_JSON_KEY" --metadata_path <scratchpad>/listing
  ```
- **Commit:**
  - `fastlane/metadata/android/<locale>/{title,short_description,full_description}.txt`, unchanged
  - the last release's notes as `changelogs/35.txt`, if Play returns them
  - the icon and feature graphic in `art/play-store/` (the old screenshots aren't kept, step 6 replaces them)
  - `fastlane/README.md`: layout, limits, locale table and setup, adapted from Cephalopod's
  - `.gitignore`: `fastlane/report.xml`
- **Locale table (to confirm with the download):**

  | App | Play |
  |---|---|
  | `en` | `en-US` or `en-GB` |
  | `ca` | `ca` |
  | `de` | `de-DE` |
  | `es` | `es-ES` |
  | `fr` | `fr-FR` |
  | `it` | `it-IT` |
  | `pt` | `pt-PT` (the app's texts read as European Portuguese) |

- **Check:** the files match Play Console, and the listing in Play Console is unchanged.

### 2. Update and complete the texts
- Review the English text against the current app and propose changes. You approve it before it is translated.
- Draft the languages that are missing. You or a native reader review them.
- **Guard:** a `StoreListingKonsistTest` in `:tools:konsist`. It then runs in `RELEASING.md` step 6.
  - Every language in `localeFilters` has its Play folder.
  - The texts fit the limits (30, 80, 4000 and 500 characters).
  - Notes from version code 36 on are a list: every line starts with `• ` (or the marker the live notes use).
- **Check:** the guard fails with a missing folder, a text that is too long and a notes paragraph, and passes otherwise.

### 3. `scripts/upload_release.sh`: bundle and notes to internal testing
- **Needs** step 0's service account access and the matching upload key.
- **Reads** the version code and name from `gradle/libs.versions.toml`.
- **Stops before uploading if:**
  - the working tree is dirty (other than `MEMORY.md`), or HEAD isn't `Version <code> (<name>) final` or isn't pushed
  - a locale has no `changelogs/<code>.txt`, or it is over 500 characters
  - the bundle is unsigned, or not signed with the key in `keystore.properties` (`keytool -printcert -jarfile`)
- **Builds** `:app:bundleRelease`. An upload also sends the mapping file to Crashlytics, which is wanted. `--stage-only` and `--validate-only` exclude `uploadCrashlyticsMappingFileRelease`.
- **Uploads** bundle and notes in one `supply` call, so one Play edit:
  ```bash
  fastlane supply --package_name gc.david.dfm --json_key "$PLAY_JSON_KEY" \
    --aab app/build/outputs/bundle/release/app-release.aab \
    --metadata_path "$stage" --track internal --release_status completed \
    --skip_upload_apk --skip_upload_metadata --skip_upload_images --skip_upload_screenshots \
    --validate_only "$validate_only"
  ```
  - `completed` on the internal track makes it available to your testers at once.
  - There is no `--track` option: it can't reach production.
- **Shared code** (version, locale table, key, flags and the terminal check) goes in `scripts/play_common.sh`. Cephalopod has a copy in each script.
- **Check:**
  - `bash -n`; each stop condition is provoked once
  - `--stage-only` output read by you
  - `--validate-only` by Claude. Version code 35 is already used, so it needs a local, uncommitted 36 and a temporary bypass of the tree check. I expect that a validated and dropped edit doesn't use up the code, but this isn't confirmed.

### 4. `scripts/upload_store_listing.sh`: texts and images
- Cephalopod's script, with the 7 locales, `--track` (default `production`) and `--version-code`.
- Uploads the texts, icon, feature graphic and screenshots with `--sync_image_upload`, so pictures Play already has aren't sent again.
- Stages each device's pictures in its `supply` folder.
- Refuses to run if a language has fewer than 4 or more than 8 pictures for any of the three devices. `supply` keeps Play's old ones for it, which would mix old and new. `--skip-images` uploads only the texts, so that they don't wait for steps 5 and 6.
- **Check:** `--stage-only`, then `--validate-only` by Claude with version code 35 on `production`: with `--skip-images` now, and with the pictures after step 6.

## Group 2: screenshots

### Devices
The script turns the one emulator into each device with `wm size` and `wm density`, as Cephalopod does for the phone, so no tablet emulators are needed. The app follows the window size: at 600 dp or wider the navigation rail is always open.

| Folder | `supply` folder | Screen | Density | Window |
|---|---|---|---|---|
| `phone` | `phoneScreenshots` | 1080 x 1920 | 420 | 411 x 731 dp, portrait |
| `tablet-7` | `sevenInchScreenshots` | 1080 x 1920 | 288 | 600 x 1067 dp, portrait |
| `tablet-10` | `tenInchScreenshots` | 1920 x 1080 | 240 | 1280 x 720 dp, landscape |

- **Play's rules (help page, read 2026-11-24):** up to 8 pictures per device; tablets want at least 4, at 16:9 or 9:16, with sides of 1080 px or more. It gives both 3840 and 7680 px as the largest side; these sizes fit either. It has no separate rules for 7" and 10". Guides give 8 MB per picture: at 2560 x 1440 four pictures went past 8,000,000 bytes, so the 10" tablet uses 1920 x 1080 at density 240, the same 1280 x 720 dp.
- **So your list of screens needs 4 to 8 entries**, the same for every device.

### 5. Screenshot scripts, on the three devices
- **Build (2026-11-25):** a `screenshots` build type, copied from release, with canned addresses and elevation, because Google's services are limited to about 100 requests a day. Release has none of it.
- **No test in the app:** `scripts/take_store_screenshots.sh` drives the installed app with `adb`, through `scripts/store_screenshots.py`. It stops unless the working tree is clean (other than `MEMORY.md`) and the last commit is the version commit.
- **Pictures:** `distance`, `start`, `points`, `elevation`, `details`, `saved-distances`, `loaded-distance`, in that order.
- **Routes:** Las Palmas de Gran Canaria to Puerto del Rosario. The elevation goes to Santa María de Guía, and the loaded distance from Auditorio Alfredo Kraus to La Puntilla.
- **Docs:** `art/README.md`.
- **Check:** the English pictures of the three devices, sent to you, with their total size. Emulator settings are back to normal, also after Ctrl-C.

### 6. Screenshots in every language
- Run it for the 7 languages and the three devices (21 pictures for each screen) and look for cut or untranslated text.
- You approve the pictures. Git ignores them, so they aren't committed.

## Group 3: documentation

### 7. `RELEASING.md`, `CLAUDE.md`, README
- **`RELEASING.md`:**
  - **Phase 1:**
    - step 1 leaves out `MEMORY.md`
    - step 5 asks for approval of two texts in every language: `whats_new_message`, a short paragraph, and `changelogs/<code>.txt`, an unordered list of up to 500 characters, both user friendly
    - the two TODOs become "update the Play Store texts if needed" and "take the screenshots again if the screens changed". The texts move before the version commit, so that the checks and the push cover them. The screenshots stay after it, as in today's step 8
  - **Upload (new, replaces your manual build and upload):**
    1. Claude runs `upload_release.sh --validate-only` and reports the result.
    2. Claude gives you the upload command, and you run it: `scripts/upload_release.sh`
    3. Your testers test it, and you promote it to production in Play Console.
    4. If the texts or pictures changed, Claude runs `upload_store_listing.sh --validate-only` and gives you its upload command: `scripts/upload_store_listing.sh`
  - **Phase 2:** unchanged: tag, merge and push when you ask.
- **`CLAUDE.md`:** one line on `scripts/`, `fastlane/` and `art/`, and the `screenshots` build type.
- **README:** a link to `RELEASING.md`.

## Verification

- `./gradlew testDebugUnitTest :tools:konsist:test lint` after steps 2, 5 and 7.
- The first real release is the end-to-end test: bundle and notes on internal in 7 languages, installed from Play, map and search working in it (API key restrictions), a Crashlytics mapping for the version, the notes kept after you promote it, and the listing updated.

## Left out

- Promoting to production and rollouts, CI, GitHub releases and a new feature graphic.
- Play Console forms that `supply` doesn't handle: Data safety, content rating and the privacy policy. The app uses location and has no privacy policy link in it, so check that before the next release.
