# This example shows how database commands can be executed.
# Documentation: http://basex.org/api
#
# (C) BaseX Team 2005-11, ISC License

import BaseXClient, time

try:
  # initialize timer
  start = time.clock()

  # create session
  session = BaseXClient.Session('localhost', 1984, 'admin', 'admin')

  # perform command and print returned string
  print session.execute("create db testDB")
  print session.info()
  
  session.add("World.xml", "", "<x>Hello World!</x>")
  print session.info()

  # close session
  session.close()

  # print time needed
  time = (time.clock() - start) * 1000
  print time, "ms."

except IOError as e:
  # print exception
  print e
