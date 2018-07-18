'''
Tools for handling bytestrings with non-printable characters
'''


class SafeString(object):
    '''
    Turns bytestrings with non-line-printable characters into strings containing hex _segments
    e.g. b'hello\nworld' -> "hello0Aworld"
    Also provides highlighting information, so that the hex _segments can be printed in different color
    '''

    def __init__(self, display_string, highlights):
        self._display_str = display_string
        self._highlights = highlights
    
    def display_length(self):
        return len(self._display_str)
    
    def display_str(self):
        return self._display_str
    
    def highlights(self):
        '''
        Return list of tuples (p, l) where p is starting position of hex-segment, l is hex-segment length
        List is empty for line-printable strings without hex-_segments
        '''
        return self._highlights

    @staticmethod
    def is_safe(c):
        '''
        Is this char displayable on console ?
        '''
        return c > 31 and c < 127
    
    @staticmethod
    def _segments(byte_string):
        segments = []
        safe = True
        segment = bytearray()
        for c in byte_string:
            csafe = SafeString.is_safe(c)
            if csafe != safe:
                segments.append((segment, safe))
                segment = bytearray()
                safe = csafe
            segment.append(c)
        segments.append((segment, safe))
        return segments
    
    @staticmethod
    def _init_display_str(segments):
        s = ""
        for segment, safe in segments:
            s += bytearray.decode(segment, "UTF-8") if safe else segment.hex().upper()
        return s
    
    @staticmethod
    def _init_highlights(segments):
        h = []
        l = 0
        for segment, safe in segments:
            segment_length = len(segment)
            if not safe:
                segment_length *= 2
                h.append((l, segment_length))
            l += segment_length
        return h
    
    def trim_left(self, length):
        new_display_str = self._display_str[-length:]
        new_highlights = self._trim_left_highlights(self.display_length(), length, self._highlights)
        return SafeString(new_display_str, new_highlights)
    
    def _trim_left_highlights(self, l, trim_length, h):
        if l > trim_length:
            h2 = []
            diff = l - trim_length
            for start, length in h:
                end = start + length
                if end <= diff:  # _segments before the trim
                    pass
                elif start < diff:  # segment overlapping
                    h2.append((0, end - diff))
                else:  # segment after trim
                    h2.append((start - diff, length))
            return h2
        else:
            return h        
    
    def trim_right(self, length):
        new_display_str = self._display_str[:length]
        new_highlights = self._trim_right_highlights(self.display_length(), length, self._highlights)
        return SafeString(new_display_str, new_highlights)

    def _trim_right_highlights(self, l, trim_length, h):
        if l > trim_length:
            h2 = []
            for start, length in h:
                end = start + length
                if end <= trim_length:  # _segments before the trim
                    h2.append((start, length))
                elif start < trim_length:  # segment overlapping
                    h2.append((start, trim_length - start))
                else:  # segment after trim
                    break
            return h2
        else:
            return h
        
    def limit_display(self, length_limit):
        orig_len = self.display_length() 
        if orig_len > length_limit:
            if length_limit < 0:
                raise "Negative length_limit"
            if length_limit < 3:
                return LimitedSafeString("." * length_limit, [], 0)
            md1, mm1 = divmod(length_limit - 2, 2)
            midmark_start = md1 + mm1
            midmark_end = midmark_start + 2
            before_midmark = self.trim_right(midmark_start)
            after_midmark = self.trim_left(md1)
            new_display_str = before_midmark.display_str() + ".." + after_midmark.display_str()
            new_highlights = before_midmark.highlights() + [(p + midmark_end, l) for (p, l) in after_midmark.highlights()]
            return LimitedSafeString(new_display_str, new_highlights, midmark_start)
        else:
            return LimitedSafeString(self._display_str, self._highlights, None)
    
    @classmethod
    def from_bytes(cls, byte_string):
        segments = SafeString._segments(byte_string)
        return SafeString(SafeString._init_display_str(segments), SafeString._init_highlights(segments))


class LimitedSafeString(SafeString):
    '''
    Limits string's display length by shortening it whereby the middle is cut out and replaced by ".." mid mark
    '''

    def __init__(self, display_string, highlights, midmark_position):
        SafeString.__init__(self, display_string, highlights)
        self._midmark = midmark_position
        
    def midmark(self):
        return self._midmark

    @classmethod
    def from_bytes(cls, byte_string, length_limit):
        return SafeString.from_bytes(byte_string).limit_display(length_limit)
