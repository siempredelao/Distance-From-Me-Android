# Play Store screenshots

How to take the screenshots of the Play Store again, for any version. They show the **release build**, and are taken **after the commit `Version <code> (<name>) final`** (see [RELEASING.md](../RELEASING.md)). Take them again when the screens have changed. `art/play-store/` holds them, and git ignores them (see [fastlane/README.md](../fastlane/README.md)).

**Draft:** the list of pictures is a proposal, and the steps are by hand until `scripts/take_store_screenshots.sh` exists.

## Pictures

Saved as `art/play-store/<language>/<device>/<n>-<screen>.png`, numbered in the order the store shows them, the same on every device. Play takes 4 to 8.

| | Screen | Shows |
|---|---|---|
| 1 | `distance` | The distance from Las Palmas de Gran Canaria to Puerto del Rosario |
| 2 | `start` | The map before measuring, with its hint |
| 3 | `points` | A distance with several points, with its hint |
| 4 | `elevation` | The elevation profile |
| 5 | `details` | The addresses and coordinates of both ends |
| 6 | `saved-distances` | The dialog with several saved distances |
| 7 | `loaded-distance` | A saved distance, in yellow with its name |

## Route

From **Las Palmas de Gran Canaria** (28.1235, -15.4363) to **Puerto del Rosario**, as in the app icon. The elevation picture goes to **Santa María de Guía** instead, because the sea has no interesting profile.

A search or a long press extends the distance on the map, so reset it first with Menu, Current position.

## Devices

One emulator, turned into each device with `wm size` and `wm density`. Never a phone: clearing the app's data deletes its saved distances.

| Folder | Size | Density |
|---|---|---|
| `phone` | 1080x1920 | 420 |
| `tablet-7` | 1080x1920 | 288 |
| `tablet-10` | 2560x1440 | 320 |

## Prepare

1. Check that the last commit is the version commit and that the working tree is clean.
2. Start an emulator with Android 13 or later and network. Build and install the release app. The exclusion keeps the mapping file from being uploaded to Crashlytics, which the release upload does. The debug app has to be uninstalled first, because it has another signature:
   ```bash
   ./gradlew :app:assembleRelease -x :app:uploadCrashlyticsMappingFileRelease
   adb uninstall gc.david.dfm
   adb install app/build/outputs/apk/release/app-release.apk
   ```
   The installed app still reports to the production Crashlytics project.
3. Clear the app, choose the language and grant what would show a dialog:
   ```bash
   adb shell pm clear gc.david.dfm
   adb shell cmd locale set-app-locales gc.david.dfm --user 0 --locales en-GB
   for p in ACCESS_FINE_LOCATION ACCESS_COARSE_LOCATION; do
     adb shell pm grant gc.david.dfm android.permission.$p
   done
   ```
4. Set the device, gesture navigation and a clean status bar:
   ```bash
   adb shell wm size 1080x1920 && adb shell wm density 420
   adb shell cmd overlay enable-exclusive --category com.android.internal.systemui.navbar.gestural
   adb shell settings put global sysui_demo_allowed 1
   demo() { adb shell am broadcast -a com.android.systemui.demo -e command "$@"; }
   demo enter
   demo clock -e hhmm 1000
   demo battery -e level 100 -e plugged false
   demo network -e fully true -e wifi show -e level 4 -e mobile hide
   demo notifications -e visible false
   ```
   After a size or density change, run `demo exit` and the `demo` lines again, or the icons appear twice.
5. Open the app. Set the position before and after, and wait for the map's tiles:
   ```bash
   adb emu geo fix -15.4363 28.1235   # longitude first
   adb shell am start -n gc.david.dfm/.onboarding.presentation.OnboardActivity
   ```

## Take them

Save each picture with `adb exec-out screencap -p > <file>.png`, once the map has no grey tiles.

1. **`start`:** as the app opens.
2. **`distance`:** search `Puerto del Rosario` and close the hint.
3. **`details`:** tap the distance's label and wait for both addresses.
4. Tap Save, type a name, hide the keyboard (it covers Accept) and tap Accept. Do the same with two other places, for `saved-distances`.
5. **`saved-distances`:** Menu, Load distance.
6. **`loaded-distance`:** choose Puerto del Rosario.
7. **`elevation`:** Menu, Current position, search `Santa María de Guía` and tap Show elevation chart.
8. **`points`:** Menu, Any position. Tap two or more places and take it before the long press that ends the distance.

The Google logo has to stay visible. The distance units follow the language's country, so `en-GB` shows miles.

## Known problems

- **API keys:** the release build has its own geocoding key, and the Maps key may be limited to certain signing certificates. If the map is blank or the searches fail, check the keys' restrictions in Google Cloud.
- **`OVER_QUERY_LIMIT`:** the Elevation API may reject a request, and then there is no chart. Wait and search again.

## Put the emulator back

```bash
adb shell wm size reset && adb shell wm density reset
adb shell am broadcast -a com.android.systemui.demo -e command exit
adb shell settings put global sysui_demo_allowed 0
adb shell cmd overlay enable-exclusive --category com.android.internal.systemui.navbar.threebutton
adb shell cmd locale set-app-locales gc.david.dfm --user 0 --locales ""
```
