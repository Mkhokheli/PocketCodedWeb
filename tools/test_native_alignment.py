import importlib.util
from pathlib import Path
import struct
import unittest

spec = importlib.util.spec_from_file_location("alignment", Path(__file__).with_name("check-native-alignment.py"))
alignment = importlib.util.module_from_spec(spec)
spec.loader.exec_module(alignment)


def elf(segments):
    data = bytearray(64 + len(segments) * 56)
    data[:6] = b"\x7fELF\x02\x01"
    struct.pack_into("<Q", data, 32, 64)
    struct.pack_into("<HH", data, 54, 56, len(segments))
    for i, (page_alignment, address) in enumerate(segments):
        struct.pack_into("<IIQQQQQQ", data, 64 + i * 56, 1, 4, 0, address, address, 0, 0, page_alignment)
    return data


class NativeAlignmentTest(unittest.TestCase):
    def test_accepts_16k_and_larger_pages(self):
        self.assertEqual([16384, 65536], alignment.check_elf(elf([(16384, 0), (65536, 65536)])))

    def test_rejects_later_misaligned_segment(self):
        with self.assertRaisesRegex(ValueError, "LOAD 1"):
            alignment.check_elf(elf([(16384, 0), (4096, 0)]))

    def test_rejects_offset_address_mismatch(self):
        with self.assertRaisesRegex(ValueError, "disagree"):
            alignment.check_elf(elf([(16384, 4096)]))

    def test_rejects_missing_or_truncated_headers(self):
        for data in (elf([]), elf([(16384, 0)])[:-1]):
            with self.assertRaises(ValueError):
                alignment.check_elf(data)

    def test_32_bit_does_not_require_16k(self):
        data = elf([(4096, 0)])
        data[4] = 1
        self.assertIsNone(alignment.check_elf(data))


if __name__ == "__main__":
    unittest.main()
