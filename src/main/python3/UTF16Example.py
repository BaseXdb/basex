# -*- coding: utf-8 -*-
# Documentation: http://docs.basex.org/wiki/Clients
#
# (C) BaseX Team 2005-12, BSD License
import BaseXClient

import sys
if sys.version < '3': # i'm testing with Python 2.7.3
    import codecs
    sys.stdout = codecs.getwriter('utf-8')(sys.stdout)

import xml.dom.minidom
# input encoding is utf-16le
doc = xml.dom.minidom.parse("UTF16example.xml")
# expat parser will decode it to real unicode, and rewrite processing instruction.
# so, we can send this (->toxml()) as content for basex, safely.
content = doc.toxml()
# str if Python 3.x, unicode if Python 2.x.
# (both are actually real unicode. (ucs2 or ucs4.))
print(type(content))

# create session
session = BaseXClient.Session('localhost', 1984, 'admin', 'admin')
try:
    # create empty database
    session.execute("create db py3clientexample")
    print(session.info())

    # add document
    session.add("py3clientexample/originally_u16le.xml", content)
    print(session.info())

    # run query on database
    query = session.query("""doc('py3clientexample')""")

    for typecode, item in query.iter():
        print("typecode=%d" % typecode)
        print("item=%s" % item)

    # drop database
    session.execute("drop db py3clientexample")
    print(session.info())

except Exception as e:
    # print exception
    print(repr(e))

finally:
    # close session
    if session:
        session.close()
