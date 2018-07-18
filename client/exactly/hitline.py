'''
Tools for hit line layout handling
'''
from exactly.safestring import SafeString


class HitLineLayout(object):
    """
    Layout for HitLine
    """
    
    def __init__(self, length, before, pattern_bytes, after, selected, color):
        self._color = color
        max_disp_pat_len = length // 3
        safe_pat = SafeString.from_bytes(pattern_bytes).limit_display(max_disp_pat_len)
        pd, pm = divmod(length - safe_pat.display_length(), 2)
        l_bef = pd + pm
        l_aft = pd
        safe_bef = SafeString.from_bytes(before).trim_left(l_bef)
        safe_aft = SafeString.from_bytes(after).trim_right(l_aft)

        pad_bef = "." * (l_bef - safe_bef.display_length())
        pad_aft = "." * (l_aft - safe_aft.display_length())

        self._display_str = pad_bef + safe_bef.display_str() + safe_pat.display_str() + safe_aft.display_str() + pad_aft

        col_pad = self._color.SEL_LINE_PAD if selected else self._color.LINE_PAD
        col_ctx = self._color.SEL_LINE_CTX if selected else self._color.LINE_CTX
        col_pat = self._color.SEL_LINE_PAT if selected else self._color.LINE_PAT
        col_ctx_spec = self._color.SEL_LINE_CTX_SPEC if selected else self._color.LINE_CTX_SPEC

        h = []
        offset = 0
        if len(pad_bef) > 0:
            h.append((offset, len(pad_bef), col_pad))
            offset += len(pad_bef)
        if safe_bef.display_length() > 0:
            h.append((offset, safe_bef.display_length(), col_ctx))
            h += [(p + offset, l, col_ctx_spec) for (p, l) in safe_bef.highlights()]
            offset += safe_bef.display_length()
        if safe_pat.display_length() > 0:
            h.append((offset, safe_pat.display_length(), col_pat))
            h += [(p + offset, l, col_ctx_spec) for (p, l) in safe_pat.highlights()]
            offset += safe_pat.display_length()
        if safe_aft.display_length() > 0:
            h.append((offset, safe_aft.display_length(), col_ctx))
            h += [(p + offset, l, col_ctx_spec) for (p, l) in safe_aft.highlights()]
            offset += safe_aft.display_length()
        if len(pad_aft) > 0:
            h.append((offset, len(pad_bef), col_pad))
            offset += len(pad_aft)

        self._highlights = h
    
    def display_str(self):
        return self._display_str
    
    def highlights(self):
        return self._highlights

