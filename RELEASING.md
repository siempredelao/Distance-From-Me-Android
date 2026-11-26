# Prepare for release
A release has two phases. Each one starts only when the user asks for it. The upload to the Play Store happens in between, through the scripts in `scripts/` (see [fastlane/README.md](fastlane/README.md)), and only the user runs a real upload.

## Phase 1: "prepare for release"
On `develop`:

1. Check that the working tree is clean, other than `MEMORY.md`, because the checks in step 7 run on it and not on the commit. If it isn't, stop and ask what to do with the changes.
2. Check whether [PRIVACY_POLICY.md](PRIVACY_POLICY.md) still describes the app, from the commits since the last `v*` tag:
   - permissions in the manifests
   - SDKs and third-party services, such as Crashlytics
   - data sent off the device, such as the addresses and coordinates in Geocoding and Elevation requests, and what is logged to Crashlytics
   - data stored on the device

   Say what you found even if nothing needs to change. If something does, suggest the text in full and a new effective date, and say whether the Data safety form in Play Console needs the same change. **Never change the policy without the user's approval.** Commit an approved change on its own, before the version commit.
3. Say whether the Play Store texts (`fastlane/metadata/android/<locale>/`) still describe the app, from the same commits. If they don't, suggest the English text and, once it is approved, translate it. Commit approved texts on their own as `Update the Play Store texts`, before the version commit.
4. Increase `appVersionCode` by 1 in `gradle/libs.versions.toml`.
5. Suggest a new `appVersionName`, with the reason, from the commits since the last `v*` tag. **Never set it without the user's approval.**
6. Write two texts from the same commits, user friendly and not too technical, and ask for their approval together with the version name:
   - `whats_new_message` (`features/about/src/main/res/values*/strings.xml`): a short paragraph, in all supported locales.
   - `fastlane/metadata/android/<locale>/changelogs/<versionCode>.txt`: the release notes, an unordered list (`- ` lines) of up to 500 characters, in all 11 locales. `en-US` copies `en-GB`, and `es-US` copies `es-419`.
7. Run `./gradlew testDebugUnitTest :tools:konsist:test lint`. If a test, a Konsist guard or lint (errors only) fails, stop and report it: don't commit, push or fix it as part of the release unless the user says so. Report lint warnings, but don't stop for them.
8. Commit the version bump and the two texts, and nothing else, as `Version <code> (<name>) final`.
9. Say whether the screens in the Play Store screenshots changed since they were last taken, from the same commits, and for which devices. Take them again only if they did, and only those (see [art/README.md](art/README.md)): run `scripts/take_store_screenshots.sh` in the background, with `--device` and the languages when not all are needed, and report only the sets it didn't finish. Don't look at every picture: the user does.
10. Push `develop` to origin, without asking again.

## Upload
1. Run `scripts/upload_release.sh --validate-only` and report the result. It checks the bundle and the notes with Play and changes nothing.
2. Give the user the upload command, and the user runs it: `scripts/upload_release.sh`. It sends the bundle and the notes to internal testing.
3. The testers test it, and the user promotes it to production in Play Console.
4. If the texts or the screenshots changed, run `scripts/upload_store_listing.sh --validate-only` (with `--skip-images` when only the texts changed) and give the user its upload command. It goes to review for all users, so it waits until the version is in production.

## Phase 2: once it is uploaded to the Play Store
1. Create the lightweight tag `v<name>` on the `Version <code> (<name>) final` commit.
2. Merge that tag, not the tip of `develop`, into `master`, so that `master` matches what was shipped: `git merge --no-ff v<name> -m "Merge branch 'develop' into master"`.
3. Push `master` and the tag to origin: `git push origin master v<name>`.
