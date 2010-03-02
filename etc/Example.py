#
# This example shows how database commands can be performed
# via the Python BaseX API.
# The outputstream in this example is a file (result.tmp).
# The result of the query against the '11MB' document will be saved into the file.
# After all, the execution time of the query will be printed.
#
# (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License

import BaseX, sys, time

# initialize timer
start = time.clock()

# output stream
#out = open('result.tmp', 'w')
out = sys.stdout

# command to be performed
#cmd = "xquery doc('11MB')//item"
cmd = "xquery 1 to 10";

try:
  # create session
  cs = BaseX.Session('localhost', 1984, 'admin', 'admin')

  # perform command; show info if something went wrong
  if not cs.execute(cmd, out):
    print cs.info()

  # print time needed
  print "\n", (time.clock() - start) * 1000, "ms."

  # close session
  cs.close()

except IOError as e:
  print e
