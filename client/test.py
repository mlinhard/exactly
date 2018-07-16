'''
Created on 13 Jul 2018

@author: mlinhard
'''
import curses
import os

from exactly.console import ExactlyConsole, HitLine
from exactly.exactly import ExactlyClient


def main_curses(stdscr):
        console = ExactlyConsole(stdscr)
        console.display_palette()
        console.main_loop()

        
def main3():
    os.environ.setdefault('ESCDELAY', '25')
    curses.wrapper(main_curses)


def main2():
    for i in range(0, 256):
        print str(i).rjust(10) + " " + chr(i)


def main4():
    safe, poses = HitLine.make_safe("fufu\nfafa\nmifo")
    print safe, poses


def main5():
    print type(u"sa")
    print type(u"sa".encode("utf-8"))
    print type(bytes(u"sa".encode("utf-8")))
    client = ExactlyClient()
    hits = client.search(u"sa")
    if hits == None:
        print "No hits"
    else:
        for hit in hits:
            print u"Pos %s: %s" % (hit.position, hit.before + u"sa" + hit.after)


def main():
    client = ExactlyClient()
    doc = client.document_by_id("/home/mlinhard/Templates/corpora/JGroups/src/org/jgroups/util/MutableDigest.java")
    with open("MutableDigest.java", "w") as f:
        f.write(doc.content)

    
if __name__ == '__main__':
    main()

