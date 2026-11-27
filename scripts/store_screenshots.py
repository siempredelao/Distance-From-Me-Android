#!/usr/bin/env python3
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
"""Takes the Play Store screenshots of one language on one device.

Run by take_store_screenshots.sh, which prepares the emulator and the app:

    store_screenshots.py SERIAL LANGUAGE DENSITY FOLDER

It finds the controls by their text in the app's strings, so it works in every
language, and taps only this app's. It stops at the first thing it can't find
rather than saving a wrong picture.
"""
import re
import struct
import subprocess
import sys
import tempfile
import time
import xml.etree.ElementTree as ElementTree
import zlib
from pathlib import Path

PACKAGE = "gc.david.dfm"
ACTIVITY = f"{PACKAGE}/.onboarding.presentation.OnboardActivity"
# The places, as app/src/screenshots/.../Place.kt has them.
LATITUDE, LONGITUDE = "28.1235", "-15.4363"  # Las Palmas de Gran Canaria
DESTINATION = "Puerto del Rosario"
OTHER_SAVED = "Maspalomas"
# The loaded distance, from one end of Las Canteras beach to the other.
LOADED_NAME = "Las Canteras"
LOADED_ENDS = ("Auditorio Alfredo Kraus", "La Puntilla")
ELEVATION_DESTINATION = "Santa Maria de Guia"  # `input text` takes only ASCII.
PICTURES = ("distance", "start", "points", "elevation", "details", "saved-distances", "loaded-distance")
UNLOADED_TILE = (222, 215, 214)
STREET_DETAIL = 2.0  # Bytes per pixel as PNG: streets take 2.2 or more, the whole world 1.5.
MARKER = re.compile(r"\d\S* (mi|yd|ft|km|m)\.? *$")

serial, language, density, folder = sys.argv[1], sys.argv[2], int(sys.argv[3]), Path(sys.argv[4])
root = Path(__file__).resolve().parent.parent


class Stop(Exception):
    pass


def adb(*arguments):
    return subprocess.run(["adb", "-s", serial, *arguments], check=True, capture_output=True).stdout


def shell(*arguments):
    return adb("shell", *arguments).decode()


def string(module, name):
    """The app's text for a string resource, in this language."""
    values = "values" if language == "en" else f"values-{language}"
    file = root / module / "src/main/res" / values / "strings.xml"
    for element in ElementTree.parse(file).getroot():
        if element.get("name") == name:
            return re.sub(r"\\(.)", r"\1", "".join(element.itertext())).strip('"')
    raise Stop(f"No string {name} in {file}")


def nodes():
    """This app's nodes on the screen: their text or description, and their bounds."""
    shell("uiautomator", "dump", "/sdcard/window.xml")
    found = []
    for node in ElementTree.fromstring(adb("exec-out", "cat", "/sdcard/window.xml")).iter("node"):
        if node.get("package") == PACKAGE:
            bounds = tuple(int(n) for n in re.findall(r"-?\d+", node.get("bounds")))
            for text in (node.get("text"), node.get("content-desc")):
                if text:
                    found.append((text.strip(), bounds))
    return found


def wait_for(matches, what, seconds=30):
    end = time.time() + seconds
    while True:
        for text, bounds in nodes():
            if matches(text):
                return text, bounds
        if time.time() > end:
            raise Stop(f"Not found on the screen: {what}")
        time.sleep(1)


def wait_for_text(text, seconds=30):
    return wait_for(lambda found: found == text, text, seconds)


def is_shown(text):
    return any(found == text for found, _ in nodes())


def tap(bounds, up=0):
    left, top, right, bottom = bounds
    y = top - up if up else (top + bottom) // 2
    shell("input", "tap", str((left + right) // 2), str(y))


def tap_text(text, seconds=30):
    tap(wait_for_text(text, seconds)[1])
    time.sleep(2)


def type_text(text):
    """Types into the focused field, and hides the keyboard, which covers controls and corrects words at Enter."""
    shell("input", "text", text.replace(" ", "%s"))
    time.sleep(2)
    if "mInputShown=true" in shell("dumpsys", "input_method"):
        shell("input", "keyevent", "BACK")
        time.sleep(1)
    wait_for_text(text, 10)


def frame():
    """The screen as width, height and RGBA bytes."""
    raw = adb("exec-out", "screencap")
    width, height = struct.unpack("<II", raw[:8])
    return width, height, raw[len(raw) - width * height * 4:]


def unloaded_share(width, height, pixels):
    tile = bytes(UNLOADED_TILE) + b"\xff"
    sample = range(0, width * height, 37)
    return sum(pixels[i * 4:i * 4 + 4] == tile for i in sample) / len(sample)


def png(width, height, pixels):
    """The screen as a PNG without alpha, as Play asks."""
    rows = bytearray()
    for y in range(height):
        row = pixels[y * width * 4:(y + 1) * width * 4]
        rgb = bytearray(width * 3)
        rgb[0::3], rgb[1::3], rgb[2::3] = row[0::4], row[1::4], row[2::4]
        rows += b"\x00" + rgb

    def chunk(kind, data):
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data))

    header = struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0)
    return (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", header) + chunk(b"IDAT", zlib.compress(bytes(rows), 9))
            + chunk(b"IEND", b""))


def picture(name, detail=0.0):
    """Saves the screen once the map has its tiles, nothing moves and it shows enough detail."""
    end = time.time() + 120
    previous = None
    while True:
        width, height, pixels = frame()
        if previous == pixels and unloaded_share(width, height, pixels) < 0.001:
            image = png(width, height, pixels)
            if len(image) >= detail * width * height:
                break
        if time.time() > end:
            raise Stop(f"The screen never settled for {name}, or never zoomed in")
        previous = pixels
        time.sleep(3)

    number = PICTURES.index(name) + 1
    (folder / f"{number}-{name}.png").write_bytes(image)
    print(f"  {number}-{name}", flush=True)


def set_position():
    adb("emu", "geo", "fix", LONGITUDE, LATITUDE)


def choose(item):
    """Chooses an item of the navigation rail, which wide screens always show."""
    if not is_shown(item):
        tap_text(string("app", "search_bar_open_menu"))
    tap_text(item)


def reset():
    choose(string("app", "navigation_drawer_starting_point_current_position_item"))
    set_position()
    time.sleep(2)


def close_hint():
    if is_shown(string("app", "map_hint_close")):
        tap_text(string("app", "map_hint_close"))


def dp(value):
    return value * density // 160


def reveal_label():
    """Drags the map down while the label is under the search bar, as it can be on a wide screen."""
    for _ in range(3):
        _, (_, top, _, _) = wait_for(MARKER.search, "the distance's label")
        bar = wait_for_text(string("app", "action_bar_search_title"))[1][3]
        missing = bar + dp(85) - top  # Room for the bar's shade and a label of two lines.
        if missing <= 0:
            return
        width, height, _ = frame()
        x, y = width // 2, height // 3
        end = y + max(missing, dp(48))  # A shorter drag, or one that starts late, is a long press.
        # Held at the end, or the map goes on sliding.
        shell(f"input motionevent DOWN {x} {y}; input motionevent MOVE {x} {end}; sleep 1; "
              f"input motionevent UP {x} {end}")
        time.sleep(3)
    raise Stop("The label stays under the search bar")


def start_search(place):
    tap_text(string("app", "action_bar_search_title"))
    type_text(place)
    shell("input", "keyevent", "ENTER")


def search(place):
    start_search(place)
    wait_for(MARKER.search, f"the distance to {place}")
    time.sleep(3)
    close_hint()
    reveal_label()


def open_details(place):
    marker = wait_for(MARKER.search, "the distance's label")
    tap(marker[1], up=dp(21))  # The label is drawn above the marker.
    save_button = string("features/show-info", "action_bar_item_save_title")
    wait_for_text(save_button)
    end = time.time() + 20
    while time.time() < end:
        # Both addresses, not the errors that replace them.
        addresses = [text for text, _ in nodes() if ", 35" in text]
        if len(addresses) == 2 and any(place in text for text in addresses):
            return
        time.sleep(1)
    raise Stop(f"No addresses for the distance to {place}")


def save(name):
    tap_text(string("features/show-info", "action_bar_item_save_title"))
    type_text(name)
    tap_text(string("features/show-info", "alias_dialog_accept"))
    shell("input", "keyevent", "BACK")
    wait_for_text(string("app", "action_bar_search_title"))


def show_elevation():
    """Opens the chart. The screenshots build only has a profile for this destination."""
    reset()
    search(ELEVATION_DESTINATION)
    close = string("features/elevation", "elevation_chart_close")
    if not is_shown(close):
        tap_text(string("app", "fab_show_chart_description"))
        wait_for_text(close)


def take_all():
    set_position()
    shell("am", "start", "-W", "-n", ACTIVITY)
    time.sleep(8)
    set_position()
    wait_for_text(string("app", "map_hint_measure_from_location"), 60)
    picture("start", STREET_DETAIL)

    search(DESTINATION)
    picture("distance")
    open_details(DESTINATION)
    picture("details")
    save(DESTINATION)
    reset()
    search(OTHER_SAVED)
    open_details(OTHER_SAVED)
    save(OTHER_SAVED)
    # In this mode the first search starts the path and the second one ends it.
    choose(string("app", "navigation_drawer_starting_point_any_position_item"))
    start_search(LOADED_ENDS[0])
    wait_for_text(string("app", "map_hint_add_or_finish"))
    search(LOADED_ENDS[1])
    open_details("Las Palmas")
    save(LOADED_NAME)

    choose(string("app", "menu_load_title"))
    wait_for_text(string("app", "dialog_load_distances_title"))
    for name in (DESTINATION, OTHER_SAVED, LOADED_NAME):
        wait_for_text(name)
    picture("saved-distances")
    tap_text(LOADED_NAME)
    wait_for(lambda text: text.startswith(LOADED_NAME) and MARKER.search(text), "the loaded distance")
    close_hint()
    time.sleep(3)
    reveal_label()
    picture("loaded-distance")

    show_elevation()
    picture("elevation")

    choose(string("app", "navigation_drawer_starting_point_any_position_item"))
    width, height, _ = frame()
    points = [(str(int(width * x)), str(int(height * y))) for x, y in ((0.35, 0.45), (0.5, 0.62), (0.7, 0.5))]
    for x, y in points[:-1]:
        shell("input", "tap", x, y)
        time.sleep(2)
    x, y = points[-1]
    shell("input", "swipe", x, y, x, y, "1000")  # A long press, which draws the distance.
    wait_for(MARKER.search, "the distance with several points")
    close_hint()
    reveal_label()
    picture("points")


if __name__ == "__main__":
    try:
        folder.mkdir(parents=True, exist_ok=True)
        take_all()
    except Stop as problem:
        screen = Path(tempfile.gettempdir()) / "store-screenshots-stopped.png"
        screen.write_bytes(adb("exec-out", "screencap", "-p"))
        sys.exit(f"{language}: {problem} (see {screen})")
    except KeyboardInterrupt:
        sys.exit(130)
    except subprocess.CalledProcessError as error:
        sys.exit(f"{language}: {' '.join(error.cmd)}: {error.stderr.decode().strip()}")
