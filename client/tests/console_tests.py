'''
Tests for console.py
'''
import unittest
import exactly
from exactly.console import HitLine, SafeString
from tests.util import dehex


class Test(unittest.TestCase):

    def test_safe_string(self):
        self.assert_ss("A[0a0d]B", "A0A0DB", [(1, 4)])
        self.assert_ss("A[0a]B", "A0AB", [(1, 2)])
        self.assert_ss("AAA[0a]BBB[00010203]CC[040506]", "AAA0ABBB00010203CC040506", [(3, 2), (8, 8), (18, 6)])
        
    def assert_ss(self, input_string, expected_display_string, expected_highlights):
        ss = SafeString.from_bytes(dehex(input_string))
        self.assertEqual(ss.display_length(), len(expected_display_string))
        self.assertEqual(ss.display_str(), expected_display_string)
        self.assertEqual(ss.highlights(), expected_highlights)
        
    def test_limited_safe_string(self):
        self.assert_lss("0123456789", 10, "0123456789", [])
        self.assert_lss("0123456789", 6, "01..89", [])
        self.assert_lss("0123456789", 5, "01..9", [])
        self.assert_lss("01[01]2[02]3[03]4567[04]89", 10, "0101..0489", [(2, 2), (6, 2)])
        self.assert_lss("01[01]2[02]3[03]4567[04]89", 8, "010..489", [(2, 1), (5, 1)])
        self.assert_lss("01[01]2[02]3[03]4567[04]89", 9, "0101..489", [(2, 2), (6, 1)])

    def assert_lss(self, input_string, length, expected_display_string, expected_highlights):
        lss = SafeString.from_bytes(dehex(input_string)).limit_display(length)
        self.assertEqual(lss.display_str(), expected_display_string)
        self.assertEqual(lss.display_length(), len(expected_display_string))
        self.assertEqual(lss.highlights(), expected_highlights)
        
    def test_right_trimmed_safe_string(self):
        self.assert_rtss("0123456789", 11, "0123456789", [])
        self.assert_rtss("0123456789", 10, "0123456789", [])
        self.assert_rtss("0123456789", 6, "012345", [])
        self.assert_rtss("0123456789", 5, "01234", [])
        self.assert_rtss("01[01]2[02]3[03]4567[04]89", 6, "010120", [(2, 2), (5, 1)])
        self.assert_rtss("01[01]2[02]3[03]4567[04]89", 5, "01012", [(2, 2)])
        
    def test_left_trimmed_safe_string(self):
        self.assert_ltss("0123456789", 11, "0123456789", [])
        self.assert_ltss("0123456789", 10, "0123456789", [])
        self.assert_ltss("0123456789", 6, "456789", [])
        self.assert_ltss("0123456789", 5, "56789", [])
        self.assert_ltss("01[01]2[02]3[03]4567[04]89", 6, "670489", [(2, 2)])
        self.assert_ltss("01[01]2[02]3[03]4567[04]89", 3, "489", [(0, 1)])
        
    def assert_rtss(self, input_string, length, expected_display_string, expected_highlights):
        rtss = SafeString.from_bytes(dehex(input_string)).trim_right(length)
        self.assertEqual(rtss.display_str(), expected_display_string)
        self.assertEqual(rtss.display_length(), len(expected_display_string))
        self.assertEqual(rtss.highlights(), expected_highlights)
        
    def assert_ltss(self, input_string, length, expected_display_string, expected_highlights):
        ltss = SafeString.from_bytes(dehex(input_string)).trim_left(length)
        self.assertEqual(ltss.display_str(), expected_display_string)
        self.assertEqual(ltss.display_length(), len(expected_display_string))
        self.assertEqual(ltss.highlights(), expected_highlights)
    
    def assert_hll(self, length, bef, pat, aft, expected_display_line, expected_highlights):
        hll = exactly.console.HitLineLayout(length, dehex(bef), dehex(pat), dehex(aft))
        self.assertEqual(hll.display_str(), expected_display_line)
        self.assertEqual(hll.highlights(), expected_highlights)
    
    def test_normal_all_fits(self):
        self.assert_hll(50, "0123456789", "0123456789", "0123456789",
            "..........012345678901234567890123456789..........", [ 
            (0, 10, HitLine.COLOR_LINE_PAD),
            (10, 10, HitLine.COLOR_LINE_CTX),
            (20, 10, HitLine.COLOR_LINE_PAT),
            (30, 10, HitLine.COLOR_LINE_CTX),
            (40, 10, HitLine.COLOR_LINE_PAD)])

    def test_normal_ctx_cut(self):
        self.assert_hll(30, "012345678901234567890123456789", "0123456789", "012345678901234567890123456789",
            "012345678901234567890123456789", [
            (0, 10, HitLine.COLOR_LINE_CTX),
            (10, 10, HitLine.COLOR_LINE_PAT),
            (20, 10, HitLine.COLOR_LINE_CTX)])


if __name__ == "__main__":
    unittest.main()
