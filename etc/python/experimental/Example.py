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
cmd = "1 to 2";

try:
  # create session
  session = BaseXClient.Session('localhost', 1984, 'admin', 'admin')
  
  query = BaseXClient.Query(session)
  if query.run(cmd):
  	print query.result()
  else:
  	print query.info()

  # close session
  session.close()

  # print time needed
  time = (time.clock() - start) * 1000
  print time, "ms."

except IOError as e:
  # print exception
  print e
