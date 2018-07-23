'''
Exactly console app code

@author: mlinhard
'''
import curses, os

from exactly.exactly import  ExactlyClient

from . import get_logger
from exactly.view import ExactlyView
from exactly.hitprovider import HitProvider


class ExactlyConsole(object):

    def __init__(self, stdscr, exactly_client, initial_stats):
        self._log = get_logger(__name__)
        self._client = exactly_client
        self._stdscr = stdscr
        self._stats = initial_stats
        self._view = ExactlyView(stdscr)
        self._h = HitProvider(exactly_client, self._view)
        self._selected_hit_idx = None
        self._displayed_hit_offset = 0

    def main_loop(self):
        self._log.debug("Entering main loop")
        self._refresh_status()
        while True:
            if self._stats.done_indexing:
                c = self._stdscr.getch()
                self._log.debug("Pressed: %i", c)
                if c == 27:
                    break  # Exit the while()
                elif c == 258:  # arrow down
                    self._cursor_down()
                elif c == 259:  # arrow up
                    self._cursor_up()
                elif c > 31 and c < 127:
                    self._pattern_append(c)
                elif c == curses.KEY_BACKSPACE:
                    self._pattern_back()
                elif c == 410:  # terminal resized
                    self._resize_view()
            else:
                self._log.debug("Indexing ... sleeping 0.5 sec before fetching new status from server")
                curses.napms(500)
                self._stats = self._client.stats()
                self._refresh_status()

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
            print("ERROR: {0}".format(e))
            return 1

    def _refresh_status(self):
        stats = self._stats
        hit = None
        if stats.done_indexing:
            status_msg = "Indexed {:,} bytes in {:,} files".format(
                stats.indexed_bytes,
                stats.indexed_files)
            if self._h.has_pattern():
                if self._h.has_hits():
                    status_msg += " - {:,} hits".format(self._h.num_hits())
                    if self._selected_hit_idx != None:
                        status_msg += " - current: {:>16,} wsize: {:>4} woffset: {:>16,}".format(self._selected_hit_idx, self._view.max_displayable_hits(), self._displayed_hit_offset)
                        hit = self._h.hit(self._selected_hit_idx)
                else:
                    status_msg += " - No hits"
                self._view.update_search_bar_pattern(self._h.pattern_bytes(), self._h.has_hits())
            else:
                self._view.update_search_bar_message("Please enter a search query")
            self._view.update_status_bar(status_msg)
        elif stats.done_loading or stats.done_crawling:
            self._view.update_status_bar("Indexing ...");
            self._view.update_search_bar_message("")
        if hit:
            self._view.update_hit_bar("pos: {:>16,} doc_id: {}".format(hit.position, hit.document_id))
        else: 
            self._view.update_hit_bar("")
                
    def _cursor_up(self):
        if not self._h.has_hits():
            return
        if self._selected_hit_idx == None:
            self._selected_hit_idx = self._view.max_displayable_hits() + self._displayed_hit_offset - 1
            new_hit = self._h.hit(self._selected_hit_idx)
            self._view.cursor_up(self._h.pattern_bytes(), None, None, new_hit)
        elif self._selected_hit_idx > 0:
            selected_line = self._selected_hit_idx - self._displayed_hit_offset
            if selected_line == 0:
                self._displayed_hit_offset -= 1
            selected_hit = self._h.hit(self._selected_hit_idx)
            self._selected_hit_idx -= 1
            new_hit = self._h.hit(self._selected_hit_idx)
            self._view.cursor_up(self._h.pattern_bytes(), selected_line, selected_hit, new_hit)
        self._log_cursor()
        self._refresh_status()
                
    def _cursor_down(self):
        if not self._h.has_hits():
            return
        if self._selected_hit_idx == None:
            self._selected_hit_idx = self._displayed_hit_offset
            new_hit = self._h.hit(self._selected_hit_idx)
            self._view.cursor_down(self._h.pattern_bytes(), None, None, new_hit)
        elif self._selected_hit_idx < self._h.num_hits() - 1:
            selected_line = self._selected_hit_idx - self._displayed_hit_offset
            if selected_line == self._view.max_displayable_hits() - 1: 
                self._displayed_hit_offset += 1
            selected_hit = self._h.hit(self._selected_hit_idx)
            self._selected_hit_idx += 1
            new_hit = self._h.hit(self._selected_hit_idx)
            self._view.cursor_down(self._h.pattern_bytes(), selected_line, selected_hit, new_hit) 
        self._log_cursor()
        self._refresh_status()
        
    def _pattern_append(self, c):
        self._h.pattern_append(c)
        self._reset_cursor()
        self._view.update_hit_table(self._h.pattern_bytes(), self._h.hits(self._displayed_hit_offset), None)
        self._refresh_status()
        
    def _pattern_back(self):
        self._h.pattern_back()
        self._reset_cursor()
        self._view.update_hit_table(self._h.pattern_bytes(), self._h.hits(self._displayed_hit_offset), None)
        self._refresh_status()

    def _reset_cursor(self):
        self._selected_hit_idx = None
        self._displayed_hit_offset = 0
        
    def _resize_view(self):
        self._view.resize()
        self._reset_cursor()
        self._view.update_hit_table(self._h.pattern_bytes(), self._h.hits(self._displayed_hit_offset), None)
        self._refresh_status()

    def _log_cursor(self):
        self._log.debug("Cursor: size: %i, index: %i, offset: %i, window: %i",
            self._h.num_hits(),
            self._selected_hit_idx,
            self._displayed_hit_offset,
            self._view.max_displayable_hits())        
