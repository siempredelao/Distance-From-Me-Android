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

# Uploads the app bundle and its release notes to Google Play's internal
# testing, in one Play edit, so both go up or neither does. Internal testing
# makes the version available to the testers at once. There is no option for
# another track: promote it to production in Play Console.
#
#   scripts/upload_release.sh
#   scripts/upload_release.sh --validate-only
#   scripts/upload_release.sh --stage-only /tmp/release
#
# --validate-only   sends everything to Play to be checked, and changes nothing.
# --stage-only DIR  builds what would be uploaded in DIR, and stops. It needs no
#                   key, and does not check the commit.
#
# The version is the one in gradle/libs.versions.toml, and its notes are
# fastlane/metadata/android/<locale>/changelogs/<versionCode>.txt. PLAY_JSON_KEY
# is the service account's key file. See fastlane/README.md.
set -euo pipefail

cd "$(dirname "$0")/.."
source scripts/play_common.sh

max_characters=500 # Play's limit for the notes of one locale.
bundle=app/build/outputs/bundle/release/app-release.aab
validate_only=false
stage_only=''

while [[ $# -gt 0 ]]; do
  case "$1" in
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

# The commit that is uploaded has to be the pushed version commit, so that the
# tag of phase 2 in RELEASING.md is on what was shipped.
check_commit() {
  # The memory hook can change MEMORY.md at any time.
  [[ -z "$(git status --porcelain -- . ':(exclude)MEMORY.md')" ]] ||
    fail 'The working tree has uncommitted changes.'
  local expected="Version $version_code ($version_name) final"
  [[ "$(git log -1 --format=%s)" == "$expected" ]] ||
    fail "The last commit is not \"$expected\"."
  git fetch --quiet origin develop
  git merge-base --is-ancestor HEAD FETCH_HEAD ||
    fail 'The last commit is not pushed to develop.'
}

# supply sends an empty text for a locale without notes, so stop first.
stage_notes() {
  local stage="$1" folder notes characters
  for folder in "$metadata_folder"/*/; do
    notes="${folder}changelogs/$version_code.txt"
    [[ -f "$notes" ]] || fail "Missing release notes: $notes"
    grep -q '[^[:space:]]' "$notes" || fail "Empty release notes: $notes"
    characters=$(LC_ALL=C.UTF-8 wc -m <"$notes")
    ((characters <= max_characters)) ||
      fail "Too long ($characters of $max_characters characters): $notes"
    mkdir -p "$stage/$(basename "$folder")/changelogs"
    cp "$notes" "$stage/$(basename "$folder")/changelogs/"
  done
}

# Only a real upload sends the mapping file to the production Crashlytics.
build_bundle() {
  local arguments=(:app:bundleRelease)
  if [[ "$validate_only" == true || -n "$stage_only" ]]; then
    arguments+=(-x :app:uploadCrashlyticsMappingFileRelease)
  fi
  ./gradlew "${arguments[@]}"
}

# A value of keystore.properties.
keystore_property() {
  sed -n "s/^$1=//p" keystore.properties
}

# The SHA-256 of the certificates in keytool's answer, one per line. keytool
# answers in the language of the system unless told otherwise.
keytool_sha256() {
  keytool -J-Duser.language=en "$@" | sed -n 's/^[[:space:]]*SHA256: //p' | sort -u
}

# Play rejects a bundle that is unsigned or signed with another key.
check_signature() {
  local store_file expected actual
  store_file=$(keystore_property storeFile)
  # Gradle reads a relative path from the app module.
  [[ "$store_file" == /* ]] || store_file="app/$store_file"
  expected=$(
    KEYSTORE_PASSWORD=$(keystore_property storePassword) \
      keytool_sha256 -list -v -keystore "$store_file" \
      -alias "$(keystore_property keyAlias)" -storepass:env KEYSTORE_PASSWORD
  ) || fail 'Could not read the key in keystore.properties.'
  [[ -n "$expected" ]] || fail 'Could not read the key in keystore.properties.'
  actual=$(keytool_sha256 -printcert -jarfile "$bundle") || fail "Could not read $bundle."
  [[ -n "$actual" ]] || fail "The bundle is not signed: $bundle"
  [[ "$actual" == "$expected" ]] ||
    fail "The bundle is not signed with the key in keystore.properties: $bundle"
}

read_version

if [[ -n "$stage_only" ]]; then
  make_empty_folder "$stage_only"
  stage_notes "$stage_only"
  build_bundle
  check_signature
  cp "$bundle" "$stage_only/"
  echo "Staged $version_name ($version_code) in $stage_only"
  exit 0
fi

require_play_access
check_commit
stage=$(mktemp -d)
trap 'rm -rf "$stage"' EXIT
stage_notes "$stage"

if [[ "$validate_only" == false ]]; then
  echo "This uploads $version_name ($version_code) to internal testing, with its notes."
  confirm_upload
fi

build_bundle
check_signature

# The flags are explicit because supply also picks up any APK, bundle or text
# it finds by itself.
fastlane supply \
  --package_name "$package_name" \
  --json_key "$PLAY_JSON_KEY" \
  --aab "$bundle" \
  --metadata_path "$stage" \
  --track internal \
  --release_status completed \
  --skip_upload_apk \
  --skip_upload_metadata \
  --skip_upload_images \
  --skip_upload_screenshots \
  --validate_only "$validate_only"
