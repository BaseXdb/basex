# -*- coding: utf-8 -*-
# This example shows how external variables can be bound to XQuery expressions.
#
# Documentation: http://docs.basex.org/wiki/Clients
#
# (C) BaseX Team 2005-12, BSD License

import BaseXClient

# create session
session = BaseXClient.Session('localhost', 1984, 'admin', 'admin')

try:
    # create query instance
    input = "declare variable $name external; for $i in 1 to 10 return element { $name } { $i }"
    query = session.query(input)

    # bind variable
    query.bind("$name", "number")

    # print result
    print(query.execute())

    # close query object
    query.close()

finally:
    # close session
    if session:
        session.close()
