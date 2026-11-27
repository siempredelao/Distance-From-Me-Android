# Play Store screenshots

How to take the Play Store screenshots again, for any version. They are taken **after the commit `Version <code> (<name>) final`** (see [RELEASING.md](../RELEASING.md)), when the screens have changed. `art/play-store/` holds them, and git ignores them (see [fastlane/README.md](../fastlane/README.md)).

## Pictures

Saved as `art/play-store/<language>/<device>/<n>-<screen>.png`, numbered in the order the store shows them, the same on every device. Play takes 4 to 8.

| | Screen | Shows |
|---|---|---|
| 1 | `distance` | The distance from Las Palmas de Gran Canaria to Puerto del Rosario, as in the app icon |
| 2 | `start` | The map before measuring, with its hint |
| 3 | `points` | A distance with several points |
| 4 | `elevation` | The elevation profile to Santa María de Guía, because the sea has no interesting one |
| 5 | `details` | The addresses and coordinates of both ends |
| 6 | `saved-distances` | The dialog with three saved distances |
| 7 | `loaded-distance` | A saved distance, in yellow with its name: Auditorio Alfredo Kraus to La Puntilla |

| Folder | Size | Density | Window |
|---|---|---|---|
| `phone` | 1080x1920 | 420 | 411x731 dp |
| `tablet-7` | 1080x1920 | 288 | 600x1067 dp |
| `tablet-10` | 1920x1080 | 240 | 1280x720 dp |

The 10" tablet must keep its 1280x720 dp window, because the app's layout depends on it. A new size needs a density that keeps it, and the script stops if it doesn't.

## Take them

1. Check out the version commit, with a clean working tree.
2. Start one emulator with Android 13 or later and network. The script never uses a phone, because it clears the app's data.
3. Run it, for every language and device or only some:
   ```bash
   scripts/take_store_screenshots.sh
   scripts/take_store_screenshots.sh --device phone en es
   ```
   One language on one device takes about 6 minutes, so all of them take about 2 hours. Take only the devices and languages whose screens changed.
4. Look at every picture.

Right after the emulator starts, it may show "System UI isn't responding", and the script stops at its first picture. Wait a minute and run it again.

It stops at the first thing it can't find, and says where it saved the screen it stopped at. A set replaces the old one only when it is complete, so run it again for the languages that are missing. The emulator is put back as it was, also after Ctrl-C.

## How it works

- `scripts/take_store_screenshots.sh` checks the commit, builds and installs the app, turns the emulator into each device, with gesture navigation and a clean status bar, and puts everything back.
- `scripts/store_screenshots.py` takes the pictures of one language. It finds the controls by their text in the app's strings, so it works in every language.
- The app is the **`screenshots` build type** (`app/src/screenshots`): the release app, with its keys and signature, but with a few places and an elevation profile of its own (`Place`, `CannedAddresses`, `CannedElevation`). So the pictures don't use up the quota of Google's services, which is about 100 requests a day. The map itself comes from Google. The list of saved distances has no rate prompt, and nothing is reported to Crashlytics. Never upload it.

To change a place, change it in `Place` and in `store_screenshots.py`. The profile's terrain is SRTM, and has to be taken again for another route.
