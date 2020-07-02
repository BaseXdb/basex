3# -*- coding: utf-8 -*-
# This example shows how database commands can be executed.
#
# Documentation: https://docs.basex.org/wiki/Clients
#
# (C) BaseX Team 2005-12, BSD License

from BaseXClient import BaseXClient
import time

# initialize timer
start = time.clock()

# create session
session = BaseXClient.Session('localhost', 1984, 'admin', 'admin')

# perform command and print returned string
print(session.execute("xquery 1 to 10"))

# close session
session.close()

# print time needed
time = (time.clock() - start) * 1000
print("%.2f ms" % time)
