# -----------------------------------------------------------------------------
#
# This example shows how BaseX commands can be performed via the Python API.
# The execution time will be printed along with the result of the command.
#
# -----------------------------------------------------------------------------
# (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
# -----------------------------------------------------------------------------

import BaseXClient, time

# initialize timer
start = time.clock()

# command to be performed
cmd = "1 to 10";

try:
  # create session
  session = BaseXClient.Session('localhost', 1984, 'admin', 'admin')
  
  # create query
  query = session.query(cmd)
  # runs the query
  if query.run():
  	if query.more():
  		print 'Result Query 1: ', query.next()
  
  query2 = session.query("11 to 12")
  if query2.run():
  	if query2.more():
  		print 'Result Query 2: ', query2.next()
  		
  if query.more():
  	print 'Result Query 1: ', query.next()
  	
  if query2.more():
  	print 'Result Query 2: ', query2.next()
  	
  # close session
  session.close()

  # print time needed
  time = (time.clock() - start) * 1000
  print time, "ms."

except IOError as e:
  # print exception
  print e
