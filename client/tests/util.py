def dehex(s):
    b = bytearray()
    hexmode = False
    hexstr = None
    for c in s:
        if c == '[':
            if hexmode:
                raise "Bracket nesting error"
            hexmode = True
            hexstr = ""
        elif c == ']':
            if not hexmode:
                raise "Bracket nesting error"
            hexmode = False
            b += bytes.fromhex(hexstr)
            hexstr = None
        else:
            if hexmode:
                hexstr += c
            else:
                b.append(ord(c[0]))
    return bytes(b)