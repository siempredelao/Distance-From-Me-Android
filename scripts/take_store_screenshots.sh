#!/usr/bin/env bash
# Copyright (c) 2026 David Aguiar Gonzalez
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Takes the Play Store screenshots on an emulator, for the languages given (by
# default, all) and every device. They show the "screenshots" build, which is the
# release app with canned addresses and elevation, so it uses up no quota.
#
#   scripts/take_store_screenshots.sh
#   scripts/take_store_screenshots.sh --device phone en es
#
# --device DEVICE  only phone, tablet-7 or tablet-10.
#
# They are taken after the version commit, so it stops unless the working tree
# is clean (other than MEMORY.md) and the last commit is
# "Version <code> (<name>) final". The languages are the app's own codes, and the
# pictures go to art/play-store/<language>/<device>/, replacing what is there
# once a device is complete. It never uses a phone, because it clears the app's
# data. See art/README.md.
set -euo pipefail

cd "$(dirname "$0")/.."
source scripts/play_common.sh

pictures_folder=art/play-store
apk=app/build/outputs/apk/screenshots/app-screenshots.apk
devices=(phone tablet-7 tablet-10)
languages=()
declare -A size_of=([phone]=1080x1920 [tablet-7]=1080x1920 [tablet-10]=1920x1080)
declare -A density_of=([phone]=420 [tablet-7]=288 [tablet-10]=240)
# The window the 10" tablet's pictures must keep, in dp, whatever its pixels:
# the layout depends on it. A new size needs a density that keeps it.
declare -A window_of=([tablet-10]=1280x720)

# Stops unless SIZE (pixels, WIDTHxHEIGHT) at DENSITY gives DEVICE's window.
check_window() {
  local device_name=$1 size=$2 density=$3 expected=${window_of[$1]:-}
  [[ -n "$expected" ]] || return 0
  [[ "$size" =~ ^[0-9]+x[0-9]+$ && "$density" =~ ^[0-9]+$ ]] ||
    fail "$device_name has no size or density to check: '$size', '$density'."
  local width=${size%x*} height=${size#*x}
  local window="$(((width * 160 + density / 2) / density))x$(((height * 160 + density / 2) / density))"
  [[ "$window" == "$expected" ]] ||
    fail "$device_name at $size, density $density, is $window dp; it must stay $expected dp."
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --device)
      [[ -n "${size_of[${2:-none}]:-}" ]] || fail '--device needs phone, tablet-7 or tablet-10.'
      devices=("$2")
      shift
      ;;
    -*)
      echo "Unknown option: $1" >&2
      exit 2
      ;;
    *) languages+=("$1") ;;
  esac
  shift
done

# The Play locale of each of the app's languages, from its listing's folder.
declare -A locale_of=()
for title in "$metadata_folder"/*/title.txt; do
  locale=$(basename "$(dirname "$title")")
  locale_of[${locale%%-*}]=$locale
done
[[ ${#languages[@]} -gt 0 ]] || languages=("${!locale_of[@]}")
for language in "${languages[@]}"; do
  [[ -n "${locale_of[$language]:-}" ]] || fail "Unknown language: $language"
done

for device_name in "${devices[@]}"; do
  check_window "$device_name" "${size_of[$device_name]}" "${density_of[$device_name]}"
done

read_version
check_version_commit

serial=${ANDROID_SERIAL:-$(adb devices | sed -n 's/^\(emulator-[0-9]*\)\tdevice$/\1/p')}
[[ "$serial" =~ ^emulator-[0-9]+$ ]] || fail 'Start one emulator, and no phone, first.'
device() { adb -s "$serial" "$@"; }
[[ "$(device shell getprop ro.boot.qemu | tr -d '\r')" == 1 ]] || fail "Not an emulator: $serial"
(($(device shell getprop ro.build.version.sdk | tr -d '\r') >= 33)) || fail 'The emulator needs Android 13 or later.'
[[ "$(device shell getprop sys.boot_completed | tr -d '\r')" == 1 ]] || fail 'The emulator has not finished booting.'

setting() { device shell settings get "$1" "$2" | tr -d '\r'; }

# Put back what was there, also after Ctrl-C. A setting that was never set is "null".
put_back() {
  if [[ "$2" == null ]]; then
    device shell settings delete global "$1" >/dev/null
  else
    device shell settings put global "$1" "$2"
  fi
}

navigation_mode=$(setting secure navigation_mode)
demo_allowed=$(setting global sysui_demo_allowed)
error_dialogs=$(setting global hide_error_dialogs)
staging=$(mktemp -d)

demo() { device shell am broadcast -a com.android.systemui.demo -e command "$@" >/dev/null; }

restore() {
  set +e
  demo exit
  device shell wm size reset
  device shell wm density reset
  put_back sysui_demo_allowed "$demo_allowed"
  put_back hide_error_dialogs "$error_dialogs"
  case "$navigation_mode" in
    0) device shell cmd overlay enable-exclusive --category com.android.internal.systemui.navbar.threebutton ;;
    1) device shell cmd overlay enable-exclusive --category com.android.internal.systemui.navbar.twobutton ;;
  esac
  device shell cmd locale set-app-locales "$package_name" --user 0 --locales '""'
  # Android takes a while, and a run started meanwhile would record the wrong mode.
  local attempt
  for attempt in {1..10}; do
    [[ "$(setting secure navigation_mode)" == "$navigation_mode" ]] && break
    sleep 1
  done
  rm -rf "$staging"
}
trap restore EXIT
trap 'exit 130' INT TERM

# A clean status bar: the time, full battery and signal, no notifications or
# carrier. A change of size repeats the icons unless the mode is left first.
clean_status_bar() {
  demo exit
  device shell settings put global sysui_demo_allowed 1
  demo enter
  demo clock -e hhmm 1000
  demo battery -e level 100 -e plugged false
  demo network -e fully true -e wifi show -e level 4 -e mobile hide
  demo notifications -e visible false
}

./gradlew :app:assembleScreenshots
# A debug app has another signature, so it has to go first.
device install -r "$apk" || {
  device uninstall "$package_name"
  device install "$apk"
}

device shell settings put global hide_error_dialogs 1
device shell cmd overlay enable-exclusive --category com.android.internal.systemui.navbar.gestural

for device_name in "${devices[@]}"; do
  device shell wm size "${size_of[$device_name]}"
  device shell wm density "${density_of[$device_name]}"
  # What the emulator applied, in case it ignored or changed a value.
  check_window "$device_name" \
    "$(device shell wm size | sed -n 's/^Override size: //p' | tr -d '\r')" \
    "$(device shell wm density | sed -n 's/^Override density: //p' | tr -d '\r')"
  sleep 10
  clean_status_bar
  for language in "${languages[@]}"; do
    echo "$device_name, $language"
    device shell pm clear "$package_name" >/dev/null
    device shell cmd locale set-app-locales "$package_name" --user 0 --locales "${locale_of[$language]}"
    for permission in ACCESS_FINE_LOCATION ACCESS_COARSE_LOCATION; do
      device shell pm grant "$package_name" "android.permission.$permission"
    done
    python3 -I scripts/store_screenshots.py "$serial" "$language" "${density_of[$device_name]}" "$staging/$language/$device_name"
    target="$pictures_folder/$language/$device_name"
    mkdir -p "$target"
    rm -f "$target"/*.png
    mv "$staging/$language/$device_name"/*.png "$target/"
  done
done
