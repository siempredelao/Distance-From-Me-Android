# Prepare for release
A release has two phases. Each one starts only when the user asks for it. Building the signed release and uploading it to the Play Store happen in between, and the user does both.

## Phase 1: "prepare for release"
On `develop`:

1. Check that the working tree is clean, because the checks in step 5 run on it and not on the commit. If it isn't, stop and ask what to do with the changes.
2. Increase `appVersionCode` by 1 in `gradle/libs.versions.toml`.
3. Suggest a new `appVersionName`, with the reason, from the commits since the last `v*` tag. **Never set it without the user's approval.**
4. Rewrite `whats_new_message` (`features/about/src/main/res/values*/strings.xml`) from the same commits, in all supported locales. Keep it user friendly and not too technical. Ask for approval of the text together with the version name.
5. Run `./gradlew testDebugUnitTest :tools:konsist:test lint`. If a test, a Konsist guard or lint (errors only) fails, stop and report it: don't commit, push or fix it as part of the release unless the user says so. Report lint warnings, but don't stop for them.
6. Commit the version bump and the texts, and nothing else, as `Version <code> (<name>) final`.
7. Update the Play Store screenshots if needed. TODO: through fastlane. Until then, only say whether they look outdated.
8. Update the Play Store texts if needed. TODO: through fastlane. Until then, only say whether they look outdated.
9. Push `develop` to origin, without asking again.

## Phase 2: once it is uploaded to the Play Store
1. Create the lightweight tag `v<name>` on the `Version <code> (<name>) final` commit.
2. Merge that tag, not the tip of `develop`, into `master`, so that `master` matches what was shipped: `git merge --no-ff v<name> -m "Merge branch 'develop' into master"`.
3. Push `master` and the tag to origin: `git push origin master v<name>`.
