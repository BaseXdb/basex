# This example shows how queries can be executed in an iterative manner.
# Documentation: http://basex.org/api
#
# (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License

import BaseXClient, time

try:
  # create session
  session = BaseXClient.Session('localhost', 1984, 'admin', 'admin')
  
  try:
    # create query instance
    input = "for $i in 1 to 10 return <xml>Text { $i }</xml>"
    query = session.query(input)
    
    # initialize query
    print query.init()

    # loop through all results
    while query.more():
      print query.next()
  
    # close query object  
    query.close()
  
  except IOError as e:
    # print exception
    print e
    
  # close session
  session.close()

except IOError as e:
  # print exception
  print e
