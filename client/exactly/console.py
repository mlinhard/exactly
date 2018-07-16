'''
Created on 13 Jul 2018

@author: mlinhard
'''
from __builtin__ import Exception
from _curses import A_REVERSE, A_BOLD, A_NORMAL
import curses

from exactly import Query, ExactlyClient
import os


class ExactlyConsole(object):
        
    '''
    classdocs
    '''
    COLOR_BARS = -1
    COLOR_PALETTE_BG = -1
    COLOR_SEARCH = -1
    COLOR_LINE_PAD = -1
    COLOR_LINE_CTX = -1
    COLOR_LINE_CTX_SPEC = -1
    COLOR_LINE_PAT = -1

    def __init__(self, stdscr, exactly_client):
        '''
        Constructor
        '''
        self.exactly_client = exactly_client
        self.stdscr = stdscr
        self.height, self.width = stdscr.getmaxyx()
        self.init_colors()
        self.status_bar = StatusBar(stdscr.subwin(1, self.width, self.height - 1, 0))
        self.hit_table = HitTable(stdscr.subwin(self.height - 2, self.width, 1, 0))
        self.search_bar = SearchBar(stdscr.subwin(1, self.width, 0, 0))
#        self.ruler = Ruler(stdscr.subwin(1, self.width, 19, 0))
        self.status_bar.msg("Bounds: %s x %s" % (self.width, self.height))
        self.stdscr.refresh()

    def display_palette(self):
        self.palette = Palette(self.stdscr.subwin(35, 148, (self.height - 35) / 2, (self.width - 148) / 2))

    def main_loop(self):
        while True:
            c = self.stdscr.getch()
            if c == 27:
                break  # Exit the while()
            elif c > 31 and c < 127:
                self.pattern_append(c)
            elif c == curses.KEY_BACKSPACE:
                self.pattern_back()

    @staticmethod
    def main_curses(stdscr):
        console = ExactlyConsole(stdscr, ExactlyClient())
        console.main_loop()

    @staticmethod
    def main():
        os.environ.setdefault('ESCDELAY', '25')
        curses.wrapper(ExactlyConsole.main_curses)
                
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
            hits = self.exactly_client.search(query.pattern, query.max_candidates, query.max_context)
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
        ExactlyConsole.COLOR_LINE_PAD = curses.color_pair(0)
        ExactlyConsole.COLOR_LINE_CTX = curses.color_pair(121)
        ExactlyConsole.COLOR_LINE_CTX_SPEC = curses.color_pair(119)
        ExactlyConsole.COLOR_LINE_PAT = curses.color_pair(185)
        ExactlyConsole.COLOR_LINE_PAT_NOT = curses.color_pair(153)


class SearchBar(object):
    
    def __init__(self, win):
        self.pattern = ""
        self.win = win
        self.width = win.getmaxyx()[1]
        self.refresh()
        
    def has_pattern(self):
        return len(self.pattern) > 0

    def refresh(self, pattern_found=False):
        self.win.bkgd(' ', ExactlyConsole.COLOR_BARS)
        self.win.clear()
        if len(self.pattern) == 0:
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


class HitLine(object):
    
    def __init__(self, query, hit, win, selected=False, allow_short_pat=False):
        self.win = win
        self.width = win.getmaxyx()[1]
        
        self.pattern_len = len(query.pattern)
        self.before_len = len(hit.before)
        self.after_len = len(hit.after)
        
        if self.pattern_len >= self.width:
            raise Exception("So far not supported")
        s, m = divmod(self.width - self.pattern_len, 2)
        self.pattern_start = s + m
        self.before_start = self.pattern_start - self.before_len
        if self.before_start < 0:
            self.before_display = hit.before[-self.before_start:]  # cut the before context in the beginning
            self.before_start = 0
            self.before_pad_len = 0
        else:
            self.before_display = hit.before
            self.before_pad_len = self.before_start 
        self.after_start = self.pattern_start + self.pattern_len
        self.after_end = self.after_start + self.after_len
        if self.after_end > self.width:
            self.after_display = hit.after[0:self.after_len - self.after_end + self.width]
            self.after_end = self.width
            self.after_pad_len = 0
        else:
            self.after_display = hit.after
            self.after_pad_len = self.width - self.after_end
        
        self.before_display, unsafe_before_chars = self.make_safe(self.before_display)
        self.after_display, unsafe_after_chars = self.make_safe(self.after_display)
        
        if self.before_pad_len > 0:
            self.safe_add(0, "." * self.before_pad_len, ExactlyConsole.COLOR_LINE_PAD)
        self.safe_add(self.before_start, self.before_display, ExactlyConsole.COLOR_LINE_CTX)
        self.safe_add(self.pattern_start, query.pattern, ExactlyConsole.COLOR_LINE_PAT)
        self.safe_add(self.after_start, self.after_display, ExactlyConsole.COLOR_LINE_CTX)
        if self.after_pad_len > 0:
            self.safe_add(self.after_end, "." * self.after_pad_len , ExactlyConsole.COLOR_LINE_PAD)
            
        if len(unsafe_before_chars) > 0:
            for pos in unsafe_before_chars:
                self.safe_add(self.before_start + pos, " ", ExactlyConsole.COLOR_LINE_CTX_SPEC)
        if len(unsafe_after_chars) > 0:
            for pos in unsafe_after_chars:
                self.safe_add(self.after_start + pos, " ", ExactlyConsole.COLOR_LINE_CTX_SPEC)
            
    def safe_add(self, pos, text, attr):
        try:
            self.win.addstr(0, pos, text, attr)
        except:
            pass
    
    @staticmethod
    def make_safe(unsafe_pattern):
        safe_pattern = ""
        unsafe_charpos = []
        i = 0;
        for c in unsafe_pattern:
            if HitLine.is_safe(ord(c)):
                safe_pattern += c
            else:
                unsafe_charpos.append(i)
                safe_pattern += " "
            i += 1
        return (safe_pattern, unsafe_charpos)
            
    @staticmethod
    def is_safe(c):
        return c > 31 and c < 127


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
        self.pattern = query.pattern
        self.max_context = query.max_context
        self.hits = hits
        self.pad = self.win.subpad(len(hits), self.width, 1, 0)
        self.hit_lines = []
        line_num = 0
        for hit in hits:
            hit_line_win = self.pad.derwin(1, self.width, line_num, 0)
            hit_line = HitLine(query, hit, hit_line_win)
            self.hit_lines.append(hit_line)
            line_num += 1
#        self.pad.refresh(0, 0, 1, 0, self.height - 2, self.width)
        self.pad.refresh()


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

            
class StatusBar(object):

    def __init__(self, status_win):
        self.win = status_win
        self.width = status_win.getmaxyx()[1]

        status_win.bkgd(' ', ExactlyConsole.COLOR_BARS)
        status_win.refresh()
        
    def msg(self, text):
        self.win.addstr(0, 1, text, A_NORMAL)
