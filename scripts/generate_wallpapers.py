#!/usr/bin/env python3
"""Generate original abstract dusk landscapes, using only Python's standard library.

Writes missing wallpaper slots only. Private JPG/PNG overrides are never modified.
These generated images, like this script, are covered by the project's MIT license.
"""
import math
from pathlib import Path
import struct
import sys
import zlib

SLOTS = tuple(i for i in range(24) if i not in (10, 14))
PALETTES = (
    ((9, 15, 43), (101, 58, 130), (235, 107, 139)),
    ((6, 25, 45), (29, 87, 114), (116, 179, 174)),
    ((22, 16, 45), (85, 62, 135), (177, 124, 195)),
    ((11, 26, 49), (53, 90, 140), (223, 168, 139)),
    ((15, 29, 46), (48, 111, 119), (162, 192, 171)),
    ((30, 19, 43), (134, 68, 102), (238, 164, 130)),
)


def chunk(kind, data):
    return struct.pack('!I', len(data)) + kind + data + struct.pack('!I', zlib.crc32(kind + data) & 0xffffffff)


def render(slot, width=960, height=540):
    top, middle, bottom = PALETTES[slot % len(PALETTES)]
    phase = slot * 0.71
    ridge = [int(height * (0.72 + 0.035 * math.sin(x / width * 13 + phase)
                          + 0.02 * math.sin(x / width * 29 + phase))) for x in range(width)]
    raw = bytearray()
    for y in range(height):
        raw.append(0)  # PNG scanline filter: None
        t = y / (height - 1)
        a, b, u = (top, middle, t / .6) if t < .6 else (middle, bottom, (t - .6) / .4)
        sky = tuple(round(a[c] * (1 - u) + b[c] * u) for c in range(3))
        for x in range(width):
            if y > ridge[x]:
                depth = (y - ridge[x]) / height
                rgb = tuple(max(0, round(v * (.24 - depth * .2))) for v in sky)
            else:
                rgb = sky
            raw.extend(rgb)
    return (b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('!2I5B', width, height, 8, 2, 0, 0, 0))
            + chunk(b'IDAT', zlib.compress(raw, 6)) + chunk(b'IEND', b''))


def main():
    if len(sys.argv) != 2:
        raise SystemExit('Usage: generate_wallpapers.py OUTPUT_DIRECTORY')
    output = Path(sys.argv[1])
    output.mkdir(parents=True, exist_ok=True)
    count = 0
    for slot in SLOTS:
        stem = f'wall_{slot:02d}'
        if any((output / (stem + ext)).exists() for ext in ('.jpg', '.jpeg', '.png', '.webp')):
            continue
        (output / (stem + '.png')).write_bytes(render(slot))
        count += 1
    print(f'Generated {count} original wallpapers; existing overrides preserved.')


if __name__ == '__main__':
    main()
