# Store listing

The texts of the Google Play listing and the notes of each release, one folder per locale, in the layout that [fastlane `supply`](https://docs.fastlane.tools/actions/upload_to_play_store/) reads:

```text
fastlane/metadata/android/<locale>/
  title.txt                     up to 30 characters
  short_description.txt         up to 80 characters
  full_description.txt          up to 4000 characters
  video.txt                     link to the promotional video
  changelogs/<versionCode>.txt  release notes, up to 500 characters
```

The pictures are in `art/play-store/`. Git ignores them because of their size, so a fresh clone doesn't have them: download them again as below.

```text
art/play-store/
  icon.png, feature-graphic.png  only `en-GB` has them on Play
  <language>/phone/<n>-<screen>.png  the screenshots, numbered in the order the store shows them
```

`<language>` is the app's own code (see [Locales](#locales)). There are no tablet screenshots on Play yet.

The files come from the live listing, downloaded on 2026-11-25, and the descriptions were rewritten after that. **The app is live: never overwrite its listing without reading it first.**

The English text is the source: when it changes, change the others with it. The texts say that the app has no ads and no accounts. If that stops being true, change them, and the Data safety form in Play Console too.

## Locales

The folder names are the codes Play Console uses, which differ from the app's own (`localeFilters` in `app/build.gradle.kts`):

| App | Play listing |
|---|---|
| `en` | `en-GB` |
| `ca` | `ca` |
| `de` | `de-DE` |
| `es` | `es-ES` |
| `fr` | `fr-FR` |
| `it` | `it-IT` |
| `pt` | `pt-PT` |

The notes of 4.0.3 (version code 35) also exist in four locales that have no listing of their own: `en-US`, `es-419`, `es-US` and `pt-BR`. Their folders hold only `changelogs/`, and every release has notes in all 11. `en-US` copies `en-GB`, and `es-US` copies `es-419`.

`StoreListingKonsistTest` in `:tools:konsist` checks the locales, the limits and that the notes are lists. A new language needs its entry there too.

## Setup

1. **Service account:** in Google Cloud, enable the "Google Play Android Developer API", create a service account with no roles and download its JSON key. One account can serve several apps.
2. **Access:** in Play Console, open Users and permissions, and give the account's email **Manage store presence** and **Release apps to testing tracks** as app permissions for Distance From Me. Don't give it Admin or any production permission.
3. **Key:** keep it outside every repository, readable only by you (`chmod 600`), and export its path from your shell profile:
   ```bash
   export PLAY_JSON_KEY="<path to the key>"
   ```
4. **fastlane:** install it with `gem install fastlane`. There is no Fastfile, Appfile or Gemfile.
5. **Check the key**, which changes nothing:
   ```bash
   fastlane run validate_play_store_json_key json_key:"$PLAY_JSON_KEY"
   ```
   It should say "Successfully established connection to Google Play Store". That proves that the key logs in, not that it reaches this app: the download below does.

If a command stops with "The caller does not have permission", a permission from step 2 is missing. Play can take a few minutes, and sometimes longer, to accept a change.

## Download the live listing

It opens a Play edit and aborts it, so nothing changes on Play. It skips the download if the folder already exists, so use a new one outside the repository:

```bash
fastlane supply init --package_name gc.david.dfm --json_key "$PLAY_JSON_KEY" --metadata_path <new folder>
```

It names the pictures by locale and number, in `<locale>/images/`. An upload leaves a text alone on Play when its file is missing.
