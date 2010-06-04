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
  query2 = session.query(cmd)
  
  # runs the first query
  if query.run():
  	# gets all results of query
  	while query.more():
  		print 'Result Query 1: ', query.next()
  else:
  	print query.info()
  
  query2 = session.query("11 to 12")
  
  # runs the second query
  if query2.run():
  	# gets first result of query2
  	if query2.more():
  		print 'Result Query 2: ', query2.next()
  else:
  	print query2.info()
  	
  i = 0
  while i < 10000000:
  	i += 1
  	
  # gets next result of query2	
  if query2.more():
  	print 'Result Query 2: ', query2.next()
  
  # closes both query objects	
  query.close()
  query2.close()
  	
  # close session
  session.close()

  # print time needed
  time = (time.clock() - start) * 1000
  print time, "ms."

except IOError as e:
  # print exception
  print e
