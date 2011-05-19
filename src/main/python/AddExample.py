# This example shows how new documents can be added.
# Documentation: http://basex.org/api
#
# (C) BaseX Team 2005-11, BSD License

import BaseXClient

try:

  # create session
  session = BaseXClient.Session('localhost', 1984, 'admin', 'admin')

  # create empty database
  session.execute("create db database")
  print session.info()
  
  # add document
  session.add("World.xml", "/world", "<x>Hello World!</x>")
  print session.info()
  
  # add document
  session.add("Universe.xml", "", "<x>Hello Universe!</x>")
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
