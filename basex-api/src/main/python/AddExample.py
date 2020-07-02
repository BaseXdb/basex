# -*- coding: utf-8 -*-
# This example shows how new documents can be added.
#
# Documentation: https://docs.basex.org/wiki/Clients
#
# (C) BaseX Team 2005-12, BSD License

from BaseXClient import BaseXClient

# create session
session = BaseXClient.Session('localhost', 1984, 'admin', 'admin')

try:

    # create empty database
    session.execute("create db database")
    print(session.info())

    # add document
    session.add("world/World.xml", "<x>Hello World!</x>")
    print(session.info())

    # add document
    session.add("Universe.xml", "<x>Hello Universe!</x>")
    print(session.info())

    # run query on database
    print("\n" + session.execute("xquery collection('database')"))

    # drop database
    session.execute("drop db database")

finally:
    # close session
    if session:
        session.close()
