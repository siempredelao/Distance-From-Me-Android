# Prepare for release
A release has two phases. Each one starts only when the user asks for it. Building the signed release and uploading it to the Play Store happen in between, and the user does both.

## Phase 1: "prepare for release"
On `develop`:

1. Check that the working tree is clean, because the checks in step 6 run on it and not on the commit. If it isn't, stop and ask what to do with the changes.
2. Check whether [PRIVACY_POLICY.md](PRIVACY_POLICY.md) still describes the app, from the commits since the last `v*` tag:
   - permissions in the manifests
   - SDKs and third-party services, such as Crashlytics
   - data sent off the device, such as the addresses and coordinates in Geocoding and Elevation requests, and what is logged to Crashlytics
   - data stored on the device

   Say what you found even if nothing needs to change. If something does, suggest the text in full and a new effective date, and say whether the Data safety form in Play Console needs the same change. **Never change the policy without the user's approval.** Commit an approved change on its own, before the version commit.
3. Increase `appVersionCode` by 1 in `gradle/libs.versions.toml`.
4. Suggest a new `appVersionName`, with the reason, from the commits since the last `v*` tag. **Never set it without the user's approval.**
5. Rewrite `whats_new_message` (`features/about/src/main/res/values*/strings.xml`) from the same commits, in all supported locales. Keep it user friendly and not too technical. Ask for approval of the text together with the version name.
6. Run `./gradlew testDebugUnitTest :tools:konsist:test lint`. If a test, a Konsist guard or lint (errors only) fails, stop and report it: don't commit, push or fix it as part of the release unless the user says so. Report lint warnings, but don't stop for them.
7. Commit the version bump and the texts, and nothing else, as `Version <code> (<name>) final`.
8. Update the Play Store screenshots if needed. TODO: through fastlane. Until then, only say whether they look outdated.
9. Update the Play Store texts if needed. TODO: through fastlane. Until then, only say whether they look outdated.
10. Push `develop` to origin, without asking again.

## Phase 2: once it is uploaded to the Play Store
1. Create the lightweight tag `v<name>` on the `Version <code> (<name>) final` commit.
2. Merge that tag, not the tip of `develop`, into `master`, so that `master` matches what was shipped: `git merge --no-ff v<name> -m "Merge branch 'develop' into master"`.
3. Push `master` and the tag to origin: `git push origin master v<name>`.
