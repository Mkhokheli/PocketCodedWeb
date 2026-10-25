"""Check 64-bit ELF LOAD segments in native libraries, APKs, or AABs.

Usage: python tools/check-native-alignment.py app/src/main/jniLibs path/to/app.aab
This checks ELF layout; runtime testing on a 16 KB device is still required.
"""
import argparse
from pathlib import Path
import struct
import zipfile


def check_elf(data):
    if data[:4] != b"\x7fELF":
        raise ValueError("not an ELF binary")
    if data[4] == 1:
        return None  # Google Play's page-size requirement concerns 64-bit ABIs.
    if data[4:6] != bytes((2, 1)):
        raise ValueError("unsupported ELF class or byte order")
    offset = struct.unpack_from("<Q", data, 32)[0]
    size, count = struct.unpack_from("<HH", data, 54)
    if size < 56 or offset + size * count > len(data):
        raise ValueError("invalid ELF program header table")
    alignments = []
    for i in range(count):
        kind, flags, file_offset, address, _, file_size, memory_size, alignment = struct.unpack_from(
            "<IIQQQQQQ", data, offset + i * size
        )
        if kind != 1:
            continue
        if alignment < 16384 or alignment & (alignment - 1):
            raise ValueError(f"LOAD {i}: alignment {alignment} is not compatible with 16 KB pages")
        if file_offset % 16384 != address % 16384:
            raise ValueError(f"LOAD {i}: file offset and virtual address disagree modulo 16 KB")
        if file_offset + file_size > len(data) or memory_size < file_size:
            raise ValueError(f"LOAD {i}: invalid segment size")
        alignments.append(alignment)
    if not alignments:
        raise ValueError("no LOAD segments")
    return alignments


def binaries(path):
    if path.is_dir():
        for library in sorted(path.rglob("*.so")):
            yield str(library), library.read_bytes()
    elif zipfile.is_zipfile(path):
        with zipfile.ZipFile(path) as archive:
            for name in sorted(archive.namelist()):
                if name.endswith(".so"):
                    yield f"{path}!{name}", archive.read(name)
    else:
        yield str(path), path.read_bytes()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("paths", nargs="+", type=Path)
    args = parser.parse_args()
    failed = False
    checked = 0
    for path in args.paths:
        for name, data in binaries(path):
            try:
                alignments = check_elf(data)
                if alignments is not None:
                    checked += 1
                    print(f"PASS {name}: {alignments}")
            except (ValueError, struct.error, IndexError) as error:
                failed = True
                print(f"FAIL {name}: {error}")
    if not checked:
        print("FAIL: no compatible 64-bit libraries found")
        failed = True
    raise SystemExit(1 if failed else 0)


if __name__ == "__main__":
    main()
