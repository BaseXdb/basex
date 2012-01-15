# This example shows how new databases can be created.
#
# Documentation: http://docs.basex.org/wiki/Clients
#
# (C) BaseX Team 2005-12, BSD License

import BaseXClient

try:

  # create session
  session = BaseXClient.Session('localhost', 1984, 'admin', 'admin')

  # create new database
  session.create("database", "<x>Hello World!</x>")
  print session.info()
  
  # run query on database
  print "\n" + session.execute("xquery doc('database')")
  
  # drop database
  session.execute("drop db database")

  # close session
  session.close()

except IOError as e:
  # print exception
  print e
