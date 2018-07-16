
import logging, sys
from logging import NullHandler

exactly_log_debug = False
exactly_log_formatter = logging.Formatter('%(asctime)s %(name)s %(levelname)s %(message)s')
exactly_log_handler = None
exactly_log_debug_file = None


def create_handler():
    if exactly_log_debug:
        handler = logging.FileHandler(exactly_log_debug_file)
        handler.setLevel(logging.DEBUG)
        handler.setFormatter(exactly_log_formatter)
        return handler
    else:
        return NullHandler()


def get_handler():
    global exactly_log_handler
    if exactly_log_handler == None:
        exactly_log_handler = create_handler()
    return exactly_log_handler


def get_logger(name):
    log = logging.getLogger(name)
    log.setLevel(logging.DEBUG)
    log.addHandler(get_handler())
    return log


def set_debug_logging(debug_log_file):
    if debug_log_file:
        global exactly_log_debug
        global exactly_log_debug_file
        exactly_log_debug = True
        exactly_log_debug_file = debug_log_file

def set_debug(debugstr):
    if debugstr:
        debug_tuple = debugstr.split(":")
        sys.path.append(debug_tuple[2])
        import pydevd
        pydevd.settrace(debug_tuple[0], debug_tuple[1])
