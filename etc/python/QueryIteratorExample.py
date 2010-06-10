# -----------------------------------------------------------------------------
#
# This example shows how results from a query can be received in an iterative
# mode via the Python API.
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
  	while query.more():
  		print '- ', query.next()
  
  	# close query object	
  	query.close()
  
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
