# This example shows how queries can be executed in an iterative manner.
# Documentation: http://basex.org/api
#
# (C) BaseX Team 2005-11, BSD License

import BaseXClient, time

try:
  # create session
  session = BaseXClient.Session('localhost', 1984, 'admin', 'admin')
  
  try:
    # create query instance
    input = "declare variable $name external; for $i in 1 to 10 return element { $name } { $i }"
    query = session.query(input)

    # bind variable
    query.bind("$name", "number")

    # initialize query
    print query.init()

    # loop through all results
    while query.more():
      print query.next()
  
    # close query object  
    print query.close()
  
  except IOError as e:
    # print exception
    print e
    
  # close session
  session.close()

except IOError as e:
  # print exception
  print e
