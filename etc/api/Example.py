#
# This example shows how database commands can be performed
# via the Python BaseX API.
#
# (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 
import BaseX, sys, time

# initialize timer
start = time.clock()

# define output stream
out = open('result.tmp', 'w')
#out = sys.stdout

# command to be performed
cmd = "xquery doc('11MB')//item"
#cmd = "xquery 1+'a'";

cs = BaseX.Session('localhost', 1984, 'admin', 'admin')
if not cs.execute(cmd, out):
  print cs.info()
cs.close()

print
print (time.clock() - start) * 1000, "ms"
