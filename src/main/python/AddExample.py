# This example shows how new documents can be added.
#
# Documentation: http://docs.basex.org/wiki/Clients
#
# (C) BaseX Team 2005-12, BSD License

import BaseXClient

try:

  # create session
  session = BaseXClient.Session('localhost', 1984, 'admin', 'admin')

  # create empty database
  session.execute("create db database")
  print session.info()
  
  # add document
  session.add("world/World.xml", "<x>Hello World!</x>")
  print session.info()
  
  # add document
  session.add("Universe.xml", "<x>Hello Universe!</x>")
  print session.info()
  
  # run query on database
  print "\n" + session.execute("xquery collection('database')")
  
  # drop database
  session.execute("drop db database")
 
  # close session
  session.close()

except IOError as e:
  # print exception
  print e
