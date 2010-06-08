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
  
  try:
  	# create and run query
  	query = session.query(cmd)
  	while query.hasNext():
  		print 'Result Query 1: ', query.next()
  		
  	# create and run query
  	query2 = session.query("11 to 15")
  	while query2.hasNext():
  		print 'Result Query 2: ', query2.next()
  
  	# closes both query objects	
  	query.close()
  	query2.close()
  
  except IOError as e:
  	# print exception
  	print e
  	
  # close session
  session.close()

  # print time needed
  time = (time.clock() - start) * 1000
  print time, "ms."

except IOError as e:
  # print exception
  print e
