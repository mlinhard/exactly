'''
Classes related to displaying stuff in terminal console via curses
'''
import curses
from _curses import A_NORMAL, A_REVERSE, A_BOLD
from exactly.hitline import HitLineLayout
from . import get_logger
from exactly.safestring import LimitedSafeString


class ExactlyView(object):
    '''
    The view is supposed to manipulate terminal console screen via curses library.
    This class and it's referred sub-objects is not supposed to hold any state other than related to view.
    '''

    def __init__(self, stdscr):
        self._stdscr = stdscr
        self._search_bar = SearchBar(stdscr)
        self._hit_table = HitTable(stdscr)
        self._status_bar = StatusBar(stdscr)
        
    def update_search_bar_pattern(self, pattern_bytes, pattern_found):
        '''
        Update string in the search bar, pattern_bytes may be empty or None, may contain non-printable chars
        If pattern_matches=False it will be highlighted in different font to provide a visual clue
        '''
        self._search_bar.update_pattern(pattern_bytes, pattern_found)
        
    def update_search_bar_message(self, message):
        '''
        Show message in the search bar, in this case it will be displayed similar to status bar but centered
        '''
        self._search_bar.update_message(message)
        
    def update_hit_table(self, pattern_bytes, hits, selected_line):
        '''
        Display given hits in the space between search and status bar (screen_height -3)
        If number of hits is greater than the screen size, others will be ignored
        '''
        self._hit_table.update(pattern_bytes, hits, selected_line)
        
    def cursor_up(self, pattern_bytes, selected_line, selected_hit, new_hit):
        '''
        Move cursor up, redraw selected line
        '''
        self._hit_table.cursor_up(pattern_bytes, selected_line, selected_hit, new_hit)

    def cursor_down(self, pattern_bytes, selected_line, selected_hit, new_hit):
        '''
        Move cursor down, redraw selected line
        '''
        self._hit_table.cursor_down(pattern_bytes, selected_line, selected_hit, new_hit)
        
    def update_status_bar(self, status_message):
        '''
        Will display given status_message in status bar
        '''
        self._status_bar.msg(status_message)
        
    def max_displayable_hits(self):
        '''
        Return number of hits that can be displayed in the hit-line view
        '''
        return self._hit_table.max_displayable_hits()
    
    def max_displayable_context(self, pattern_length):
        '''
        Return maximum number of bytes of context that can be displayed before or after the pattern in the hit-table
        if the pattern has given length
        '''
        return self._hit_table.max_displayable_context(pattern_length)

    def resize(self):
        '''
        Resize all of the view elements without refreshing the data. Data must be supplied in a following call 
        to update* methods
        '''
        new_height, new_width = self._stdscr.getmaxyx()
        self._stdscr.resize(new_height, new_width)
        self._stdscr.erase()
        self._stdscr.refresh()
        self._search_bar = SearchBar(self._stdscr)
        self._hit_table = HitTable(self._stdscr)
        self._status_bar = StatusBar(self._stdscr)


class Colors(object):
    
    _INSTANCE = None
    
    def __init__(self, mock):
        if mock:
            self.PALETTE_BG = 1
            self.BARS = 2
            self.SEARCH = 3
            self.LINE_PAT = 4
            self.LINE_PAD = 5
            self.LINE_CTX = 6
            self.LINE_CTX_SPEC = 7
            self.LINE_PAT = 8
            self.SEL_LINE_PAD = 9
            self.SEL_LINE_CTX = 10
            self.SEL_LINE_CTX_SPEC = 11
            self.SEL_LINE_PAT = 12
        else:
            i = 1
            for fg in range(0, 16):
                for bg in range(0, 16):
                    curses.init_pair(i, fg, bg)
                    i += 1
            self.PALETTE_BG = -1
            self.BARS = curses.color_pair(136)
            self.SEARCH = curses.color_pair(0)
            self.LINE_PAT_NOT = curses.color_pair(153)
            self.LINE_PAD = curses.color_pair(129)
            self.LINE_CTX = curses.color_pair(121)
            self.LINE_CTX_SPEC = curses.color_pair(119)
            self.LINE_PAT = curses.color_pair(185)
    
            self.SEL_LINE_PAD = curses.color_pair(130)
            self.SEL_LINE_CTX = curses.color_pair(114)
            self.SEL_LINE_CTX_SPEC = curses.color_pair(126)
            self.SEL_LINE_PAT = curses.color_pair(178)

    @classmethod
    def instance(cls):
        if cls._INSTANCE == None:
            _INSTANCE = cls(False)
        return _INSTANCE
    
    @classmethod
    def mock(cls):
        if cls._INSTANCE == None:
            _INSTANCE = cls(True)
        return _INSTANCE


class SearchBar(object):
    
    def __init__(self, parent_win):
        self._width = parent_win.getmaxyx()[1]
        self._win = parent_win.subwin(1, self._width, 0, 0)
        self._color = Colors.instance()
        self._log = get_logger(__name__)
        
    def update_pattern(self, pattern_bytes, pattern_found):
        limited_pattern = LimitedSafeString.from_bytes(pattern_bytes, self._width)
        self._print_mid(limited_pattern.display_str(), self._color.LINE_PAT if pattern_found else self._color.LINE_PAT_NOT)
        
    def update_message(self, message):
        self._print_mid(message, 0)

    def _print_mid(self, msg, color):
        self._win.bkgd(' ', self._color.BARS)
        self._win.erase()
        try:
            s, m = divmod(self._width - len(msg), 2)
            self._win.addstr(0, s + m, msg, color | A_NORMAL)
        except:
            self._log.debug("Error while printing '%s'", msg, exc_info=True)
        self._win.refresh()
    
    
class HitTable(object):

    def __init__(self, parent_win):
        self._log = get_logger(__name__)
        parent_height, parent_width = parent_win.getmaxyx()
        self._win = parent_win.subwin(parent_height - 3, parent_width, 1, 0)
        self._color = Colors.instance()
        self._height, self._width = self._win.getmaxyx()
        self._win.bkgd(' ', self._color.SEARCH)
        self._win.idlok(True)
    
    def cursor_down(self, pattern_bytes, selected_line, selected_hit, new_hit):
        if selected_hit == None or selected_line == None:
            new_line = 0
            self._update_line(pattern_bytes, new_hit, new_line, new_line)
        elif selected_line == self._height - 1:
            self._safe_scroll(1)
            self._update_line(pattern_bytes, selected_hit, selected_line - 1, selected_line)
            self._update_line(pattern_bytes, new_hit, selected_line, selected_line)
        else:
            new_line = selected_line + 1
            self._update_line(pattern_bytes, selected_hit, selected_line, new_line)
            self._update_line(pattern_bytes, new_hit, new_line, new_line)
        self._win.refresh()

    def cursor_up(self, pattern_bytes, selected_line, selected_hit, new_hit):
        if selected_hit == None or selected_line == None:
            new_line = self._height - 1
            self._update_line(pattern_bytes, new_hit, new_line, new_line)
        elif selected_line == 0:
            self._safe_scroll(-1)
            self._update_line(pattern_bytes, selected_hit, selected_line + 1, selected_line)
            self._update_line(pattern_bytes, new_hit, selected_line, selected_line)
        else:
            new_line = selected_line - 1
            self._update_line(pattern_bytes, selected_hit, selected_line, new_line)
            self._update_line(pattern_bytes, new_hit, new_line, new_line)
        self._win.refresh()

    def update(self, pattern_bytes, hits, selected_line):
        self._win.bkgd(' ', self._color.SEARCH)
        self._win.erase()
        for line_num, hit in enumerate(hits[:self.max_displayable_hits()]):
            self._update_line(pattern_bytes, hit, line_num, selected_line)
        self._win.refresh()
        
    def _update_line(self, pattern_bytes, hit, line_num, selected_line):
        layout = HitLineLayout(self._width, hit.before, pattern_bytes, hit.after, line_num == selected_line, self._color)
        s = layout.display_str()
        for (p, l, c) in layout.highlights():
            self._safe_line_print(line_num, p, s[p:p + l], c)
        self._log.debug("Updating line %i, selected %s", line_num, line_num == selected_line)
    
    def _safe_line_print(self, line_num, pos, text, attr):
        try:
            self._win.addstr(line_num, pos, text, attr)
        except:
            self._log.debug("Problem while printing text '%s' on line %i", text, line_num, exc_info=True)

    def _safe_scroll(self, lines):
        try:
            self._win.scrollok(True)
            self._win.scroll(lines)
            self._win.scrollok(False)
        except:
            self._log.debug("Problem while scrolling by %i", lines, exc_info=True)

    def max_displayable_hits(self):
        return self._height

    def max_displayable_context(self, pattern_length):
        d, m = divmod(self._width - pattern_length, 2)
        return d + m
    
            
class StatusBar(object):

    def __init__(self, parent_win):
        parent_height, parent_width = parent_win.getmaxyx()
        self._win = parent_win.subwin(1, parent_width, parent_height - 1, 0)
        self._width = self._win.getmaxyx()[1]
        self._color = Colors.instance()
        self._win.bkgd(' ', self._color.BARS)
        self._log = get_logger(__name__)
        
    def msg(self, text):
        self._win.erase()
        try:
            self._win.addstr(0, 1, text, A_NORMAL)
        except:
            self._log.debug("Error while printing '%s'", text, exc_info=True)        
        self._win.refresh()


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
        self._color = Colors.instance()
        self.palette_win.bkgd(' ', self._color.PALETTE_BG)
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
