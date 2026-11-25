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

# Uploads the Play Store listing, its texts and pictures, in every language.
# THE LISTING IS LIVE: what it uploads goes to review for all users, whatever
# the track. Run it once the version it describes is in production.
#
#   scripts/upload_store_listing.sh
#   scripts/upload_store_listing.sh --skip-images
#   scripts/upload_store_listing.sh --validate-only
#   scripts/upload_store_listing.sh --stage-only /tmp/listing
#
# --skip-images     uploads only the texts.
# --track TRACK     a track that holds the version (default: production).
# --version-code N  a version on that track (default: the one in
#                   gradle/libs.versions.toml).
# --validate-only   sends everything to Play to be checked, and changes nothing.
# --stage-only DIR  builds the folder that would be uploaded in DIR, and stops.
#                   It needs no key.
#
# The track and the version change nothing: supply looks for the release even
# when it uploads no bundle, and stops with "Could not find release" if there is
# none. PLAY_JSON_KEY is the service account's key file. See fastlane/README.md.
set -euo pipefail

cd "$(dirname "$0")/.."
source scripts/play_common.sh

pictures_folder=art/play-store
default_locale=en-GB # The only locale with an icon and a feature graphic.
min_pictures=4
max_pictures=8 # Play's limits for the screenshots of one device.
texts=(title short_description full_description)
# Our folder for each device, and supply's.
declare -A supply_folder_of=(
  [phone]=phoneScreenshots
  [tablet-7]=sevenInchScreenshots
  [tablet-10]=tenInchScreenshots
)

track=production
requested_version_code=''
validate_only=false
stage_only=''
skip_images=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --track)
      track="${2:?--track needs a track name}"
      shift
      ;;
    --version-code)
      requested_version_code="${2:?--version-code needs a number}"
      shift
      ;;
    --skip-images) skip_images=true ;;
    --validate-only) validate_only=true ;;
    --stage-only)
      stage_only="${2:?--stage-only needs a folder}"
      # The script may be run from another folder than the root.
      [[ "$stage_only" == /* ]] || fail '--stage-only needs an absolute path.'
      shift
      ;;
    *)
      echo "Unknown option: $1" >&2
      exit 2
      ;;
  esac
  shift
done
[[ "$validate_only" == false || -z "$stage_only" ]] ||
  fail '--validate-only and --stage-only cannot be used together.'

# The locales with a listing. The others only have release notes, and staging
# them would make supply create a listing for them.
listing_locales() {
  local text
  for text in "$metadata_folder"/*/*.txt; do
    basename "$(dirname "$text")"
  done | sort -u
}

stage_texts() {
  local stage="$1" locale="$2" text
  mkdir -p "$stage/$locale"
  for text in "${texts[@]}"; do
    grep -q '[^[:space:]]' "$metadata_folder/$locale/$text.txt" ||
      fail "Missing or empty text: $metadata_folder/$locale/$text.txt"
  done
  cp "$metadata_folder/$locale"/*.txt "$stage/$locale/"
}

# supply keeps Play's own screenshots for a device without any here, which would
# mix old and new ones, so every device needs them.
stage_screenshots() {
  local stage="$1" locale="$2" device folder pictures
  for device in "${!supply_folder_of[@]}"; do
    # The app's language is the locale without its region.
    folder="$pictures_folder/${locale%%-*}/$device"
    pictures=("$folder"/*.png)
    [[ -f "${pictures[0]}" ]] || fail "No screenshots in $folder"
    ((${#pictures[@]} >= min_pictures && ${#pictures[@]} <= max_pictures)) ||
      fail "${#pictures[@]} screenshots, not $min_pictures to $max_pictures, in $folder"
    mkdir -p "$stage/$locale/images/${supply_folder_of[$device]}"
    cp "${pictures[@]}" "$stage/$locale/images/${supply_folder_of[$device]}/"
  done
}

# Exact names, because the folder can hold drafts of them too.
stage_graphics() {
  local images="$1/$default_locale/images" picture
  for picture in icon feature-graphic; do
    [[ -f "$pictures_folder/$picture.png" ]] || fail "Missing picture: $pictures_folder/$picture.png"
  done
  cp "$pictures_folder/icon.png" "$images/icon.png"
  cp "$pictures_folder/feature-graphic.png" "$images/featureGraphic.png"
}

stage_listing() {
  local stage="$1" locale
  for locale in $(listing_locales); do
    stage_texts "$stage" "$locale"
    if [[ "$skip_images" == false ]]; then
      stage_screenshots "$stage" "$locale"
    fi
  done
  [[ -d "$stage/$default_locale" ]] || fail "No texts for $default_locale in $metadata_folder"
  if [[ "$skip_images" == false ]]; then
    stage_graphics "$stage"
  fi
}

read_version
version_code="${requested_version_code:-$version_code}"

if [[ -n "$stage_only" ]]; then
  make_empty_folder "$stage_only"
  stage_listing "$stage_only"
  echo "Staged in $stage_only"
  exit 0
fi

require_play_access
stage=$(mktemp -d)
trap 'rm -rf "$stage"' EXIT
stage_listing "$stage"

if [[ "$validate_only" == false ]]; then
  echo "This changes the live listing of $package_name in every language."
  confirm_upload
fi

image_arguments=(--sync_image_upload) # Pictures Play already has are not sent again.
if [[ "$skip_images" == true ]]; then
  image_arguments=(--skip_upload_images --skip_upload_screenshots)
fi

# Skipping the changelogs is what keeps the release's notes: supply would send
# empty ones.
fastlane supply \
  --package_name "$package_name" \
  --json_key "$PLAY_JSON_KEY" \
  --metadata_path "$stage" \
  --track "$track" \
  --version_code "$version_code" \
  --skip_upload_apk \
  --skip_upload_aab \
  --skip_upload_changelogs \
  "${image_arguments[@]}" \
  --validate_only "$validate_only"
