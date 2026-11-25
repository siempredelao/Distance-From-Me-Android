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

# Shared by the scripts that upload to Google Play. Source it from the root of
# the repository, with `set -euo pipefail` already on.

package_name=gc.david.dfm
metadata_folder=fastlane/metadata/android

# supply reads its options from these too, so one set in the shell could change
# what a script sends, or to which track.
unset "${!SUPPLY_@}"

fail() {
  echo "$*" >&2
  exit 1
}

# Sets version_code and version_name, from gradle/libs.versions.toml.
read_version() {
  version_code=$(sed -n 's/^appVersionCode = "\([0-9]*\)"$/\1/p' gradle/libs.versions.toml)
  version_name=$(sed -n 's/^appVersionName = "\(.*\)"$/\1/p' gradle/libs.versions.toml)
  [[ -n "$version_code" && -n "$version_name" ]] ||
    fail 'Could not read the version from gradle/libs.versions.toml.'
}

# Stops unless fastlane and the service account's key are there.
require_play_access() {
  command -v fastlane >/dev/null || fail 'fastlane is not installed: see fastlane/README.md.'
  [[ -n "${PLAY_JSON_KEY:-}" ]] || fail 'Set PLAY_JSON_KEY to the service account key file.'
  [[ -r "$PLAY_JSON_KEY" ]] || fail "Cannot read the key file: $PLAY_JSON_KEY"
}

# Stops unless someone at a terminal types the version name.
confirm_upload() {
  [[ -t 0 && -t 1 ]] || fail 'An upload has to be run in a terminal.'
  local answer
  read -r -p "Type the version name ($version_name) to upload: " answer
  [[ "$answer" == "$version_name" ]] || fail 'Not uploaded.'
}

# Prepares the folder for --stage-only, which must not have anything in it.
make_empty_folder() {
  mkdir -p "$1"
  [[ -z "$(ls -A "$1")" ]] || fail "The folder is not empty: $1"
}
