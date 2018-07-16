'''
Exactly console app code

@author: mlinhard
'''
from _curses import A_REVERSE, A_BOLD, A_NORMAL
import curses, os

from exactly.exactly import Query, ExactlyClient

from . import get_logger
from time import sleep


class ExactlyConsole(object):

    '''
    classdocs
    '''
    COLOR_BARS = -1
    COLOR_PALETTE_BG = -1
    COLOR_SEARCH = -1

    def __init__(self, stdscr, exactly_client, initial_stats):
        '''
        Constructor
        '''
        self.log = get_logger(__name__)
        self.exactly_client = exactly_client
        self.stdscr = stdscr
        self.stats = initial_stats
        self.height, self.width = stdscr.getmaxyx()
        self.init_colors()
        self.status_bar = StatusBar(stdscr.subwin(1, self.width, self.height - 1, 0))
        self.hit_table = HitTable(stdscr.subwin(self.height - 2, self.width, 1, 0))
        self.search_bar = SearchBar(stdscr.subwin(1, self.width, 0, 0))
        self.refresh_status()
        self.stdscr.refresh()

    def refresh_status(self):
        stats = self.stats
        if stats.done_indexing:
            self.status_bar.msg("Indexed %d bytes in %d files" % (stats.indexed_bytes, stats.indexed_files))
            self.search_bar.refresh(True, False)
        elif stats.done_loading or stats.done_crawling:
            self.status_bar.msg("Indexing ...");
            self.search_bar.refresh(False, False)

    def display_palette(self):
        self.palette = Palette(self.stdscr.subwin(35, 148, (self.height - 35) / 2, (self.width - 148) / 2))

    def main_loop(self):
        self.log.debug("Entering main loop")
        while True:
            if self.stats.done_indexing:
                c = self.stdscr.getch()
                if c == 27:
                    break  # Exit the while()
                elif c > 31 and c < 127:
                    self.pattern_append(c)
                elif c == curses.KEY_BACKSPACE:
                    self.pattern_back()
            else:
                self.log.debug("Indexing ... sleeping 0.5 sec before fetching new status from server")
                sleep(0.5)
                self.stats = self.exactly_client.stats()
                self.refresh_status()
                self.stdscr.refresh()

    @staticmethod
    def main_curses(stdscr, client, stats):
        os.environ.setdefault('ESCDELAY', '25')
        console = ExactlyConsole(stdscr, client, stats)
        console.main_loop()

    @staticmethod
    def main():
        client = ExactlyClient()
        log = get_logger(__name__) 

        try:
            stats = client.stats()
            curses.wrapper(ExactlyConsole.main_curses, client, stats)
            return 0
        except KeyboardInterrupt as e:
            log.debug("Keyboard interrupt", e)
            return 0
        except Exception as e:
            log.error("An unhandled error occured", exc_info=True)
            print(e.message)
            return 1
                
    def pattern_append(self, c):
        self.search_bar.pattern_append(c)
        self.refresh_hits()
        self.search_bar.refresh(self.hit_table.has_hits())
        
    def pattern_back(self):
        self.search_bar.pattern_back()
        self.refresh_hits()
        self.search_bar.refresh(self.hit_table.has_hits())

    def refresh_hits(self):
        if self.search_bar.has_pattern():
            query = Query(self.search_bar.pattern, self.hit_table.height, self.search_bar.max_displayable_context())
            hits = self.exactly_client.search(query.pattern.encode("utf-8"), query.max_candidates, query.max_context)
            self.hit_table.display_hits(query, hits)
        else:
            self.hit_table.clear()

    def display_hits(self, query, hits):
        self.hit_table.display_hits(query, hits)
        
    def init_colors(self):
        i = 1
        for fg in range(0, 16):
            for bg in range(0, 16):
                curses.init_pair(i, fg, bg)
                i += 1
        ExactlyConsole.COLOR_PALETTE_BG = -1
        ExactlyConsole.COLOR_BARS = curses.color_pair(136)
        ExactlyConsole.COLOR_SEARCH = curses.color_pair(0)
        ExactlyConsole.COLOR_LINE_PAT_NOT = curses.color_pair(153)
        HitLine.COLOR_LINE_PAD = curses.color_pair(0)
        HitLine.COLOR_LINE_CTX = curses.color_pair(121)
        HitLine.COLOR_LINE_CTX_SPEC = curses.color_pair(119)
        HitLine.COLOR_LINE_PAT = curses.color_pair(185)


class SearchBar(object):
    
    def __init__(self, win):
        self.pattern = ""
        self.win = win
        self.width = win.getmaxyx()[1]
        self.refresh(False)
        
    def has_pattern(self):
        return len(self.pattern) > 0

    def refresh(self, done_indexing=False, pattern_found=False):
        self.win.bkgd(' ', ExactlyConsole.COLOR_BARS)
        self.win.clear()
        if len(self.pattern) == 0:
            if done_indexing:
                self.print_mid("Please enter search query", 0)
        else:
            color = ExactlyConsole.COLOR_LINE_PAT if pattern_found else ExactlyConsole.COLOR_LINE_PAT_NOT
            self.print_mid(self.pattern, color) 
            
        self.win.refresh()

    def print_mid(self, msg, color):
        s, m = divmod(self.width - len(msg), 2)
        self.win.addstr(0, s + m, msg, color | A_NORMAL)
        
    def pattern_append(self, c):
        self.pattern += chr(c)

    def pattern_back(self):
        self.pattern = self.pattern[0:-1]

    def max_displayable_context(self):
        r, m = divmod(self.width - len(self.pattern), 2)
        return r + m


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
        
    def limit_display(self, length):
        orig_len = self.display_length() 
        if orig_len > length:
            if length < 0:
                raise "Negative length"
            if length < 3:
                return LimitedSafeString("." * length, [], 0)
            md1, mm1 = divmod(length - 2, 2)
            midmark_start = md1 + mm1
            midmark_end = midmark_start + 2
            before_midmark = self.trim_right(midmark_start)
            after_midmark = self.trim_left(md1)
            new_display_str = before_midmark.display_str() + ".." + after_midmark.display_str()
            new_highlights = before_midmark.highlights() + [(p + midmark_end, l) for (p, l) in after_midmark.highlights()]
            return LimitedSafeString(new_display_str, new_highlights, midmark_start)
        else:
            return LimitedSafeString(self._display_str, self._highlights, None)
    
    @staticmethod
    def from_bytes(byte_string):
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


class HitLineLayout(object):
    """
    Layout for HitLine
    """
    
    def __init__(self, length, before, pattern, after):
        max_disp_pat_len = length // 3
        safe_pat = SafeString.from_bytes(pattern).limit_display(max_disp_pat_len)
        pd, pm = divmod(length - safe_pat.display_length(), 2)
        l_bef = pd + pm
        l_aft = pd
        safe_bef = SafeString.from_bytes(before).trim_left(l_bef)
        safe_aft = SafeString.from_bytes(after).trim_right(l_aft)

        pad_bef = "." * (l_bef - safe_bef.display_length())
        pad_aft = "." * (l_aft - safe_aft.display_length())

        self._display_str = pad_bef + safe_bef.display_str() + safe_pat.display_str() + safe_aft.display_str() + pad_aft

        h = []
        offset = 0
        if len(pad_bef) > 0:
            h.append((offset, len(pad_bef), HitLine.COLOR_LINE_PAD))
            offset += len(pad_bef)
        if safe_bef.display_length() > 0:
            h.append((offset, safe_bef.display_length(), HitLine.COLOR_LINE_CTX))
            h += [(p + offset, l, HitLine.COLOR_LINE_CTX_SPEC) for (p, l) in safe_bef.highlights()]
            offset += safe_bef.display_length()
        if safe_pat.display_length() > 0:
            h.append((offset, safe_pat.display_length(), HitLine.COLOR_LINE_PAT))
            h += [(p + offset, l, HitLine.COLOR_LINE_CTX_SPEC) for (p, l) in safe_pat.highlights()]
            offset += safe_pat.display_length()
        if safe_aft.display_length() > 0:
            h.append((offset, safe_aft.display_length(), HitLine.COLOR_LINE_CTX))
            h += [(p + offset, l, HitLine.COLOR_LINE_CTX_SPEC) for (p, l) in safe_aft.highlights()]
            offset += safe_aft.display_length()
        if len(pad_aft) > 0:
            h.append((offset, len(pad_bef), HitLine.COLOR_LINE_PAD))
            offset += len(pad_aft)

        self._highlights = h
    
    def display_str(self):
        return self._display_str
    
    def highlights(self):
        return self._highlights


class HitLine(object):
    COLOR_LINE_PAD = -1
    COLOR_LINE_CTX = -1
    COLOR_LINE_CTX_SPEC = -1
    COLOR_LINE_PAT = -1

    def __init__(self, pattern_bytes, hit, win, selected=False, allow_short_pat=False):
        self.log = get_logger(__name__)
        self.win = win
        self.width = win.getmaxyx()[1]
        
        layout = HitLineLayout(self.width, hit.before, pattern_bytes, hit.after)
        s = layout.display_str()
        for (p, l, c) in layout.highlights():
            self.safe_add(p, s[p:p + l], c)
    
    def safe_add(self, pos, text, attr):
        try:
            self.win.addstr(0, pos, text, attr)
        except Exception as e:
            self.log.debug("Some problem", e)


class HitTable(object):

    def __init__(self, win):
        self.win = win
        self.height, self.width = win.getmaxyx()
        self.win.bkgd(' ', ExactlyConsole.COLOR_SEARCH)
        self.win.refresh()

    def has_hits(self):
        return self.hits != None and len(self.hits) > 0
    
    def clear(self):
        self.hits = None
        self.win.bkgd(' ', ExactlyConsole.COLOR_SEARCH)
        self.win.erase()
        self.win.refresh()

    def display_hits(self, query, hits):
        self.clear()
        self.pattern_bytes = query.pattern.encode("utf-8")
        self.max_context = query.max_context
        self.hits = hits
        self.pad = self.win.subpad(len(hits), self.width, 1, 0)
        self.hit_lines = []
        line_num = 0
        for hit in hits:
            hit_line_win = self.pad.derwin(1, self.width, line_num, 0)
            hit_line = HitLine(self.pattern_bytes, hit, hit_line_win)
            self.hit_lines.append(hit_line)
            line_num += 1
        self.pad.refresh()

            
class StatusBar(object):

    def __init__(self, win):
        self.win = win
        self.width = win.getmaxyx()[1]
        win.bkgd(' ', ExactlyConsole.COLOR_BARS)
        self.refresh()
        
    def msg(self, text):
        self.win.addstr(0, 1, text, A_NORMAL)
        self.refresh()
        
    def refresh(self):
        self.win.refresh()


class Ruler(object):

    def __init__(self, ruler_win):
        self.win = ruler_win
        w = ruler_win.getmaxyx()[1]
        n, m = divmod(w, 10)
        for i in range(0, n):
            self.safe_add(i * 10, str((i + 1) * 10).rjust(10), A_NORMAL if i % 2 == 0 else A_REVERSE)
        if m != 0:
            self.safe_add(n * 10, " ".rjust(w - n * 10), A_NORMAL if n % 2 == 0 else A_REVERSE)

    def safe_add(self, pos, text, attr):
        try:
            self.win.addstr(0, pos, text, attr)
        except:
            pass


class Palette(object):

    def __init__(self, palette_win):
        self.palette_win = palette_win
        self.palette_win.bkgd(' ', ExactlyConsole.COLOR_PALETTE_BG)
        self.palette_win.refresh()

        i = 1
        for fg in range(0, 16):
            for bg in range(0, 16):
                try: 
                    sub = palette_win.derwin(1, 9, 1 + fg, 2 + bg * 9)
                    sub.bkgd(' ', curses.color_pair(i))
                    try:
                        sub.addstr(0, 0, str(i).rjust(3) + " " + str(fg).rjust(2) + " " + str(bg).rjust(2), A_NORMAL)
                    except:
                        pass
                    sub.refresh()
                except Exception as e:
                    raise Exception("Problem with fg: %s, bg %s: %s" % (fg, bg, e.message))
                i += 1
        i = 1    
        for fg in range(0, 16):
            for bg in range(0, 16):
                try: 
                    sub = palette_win.derwin(1, 9, fg + 18, 2 + bg * 9)
                    sub.bkgd(' ', curses.color_pair(i))
                    try:
                        sub.addstr(0, 0, str(i).rjust(3) + " " + str(fg).rjust(2) + " " + str(bg).rjust(2), A_BOLD)
                    except:
                        pass
                    sub.refresh()
                except Exception as e:
                    raise Exception("Problem with fg: %s, bg %s: %s" % (fg, bg, e.message))
                i += 1
            
        self.palette_win.refresh()
