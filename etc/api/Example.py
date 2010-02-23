# This example shows how database commands can be performed via the Python API.
# (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 
import ClientSession, sys, timeit

# initialize timer
t = timeit.Timer()

# define output stream
out = open('result.tmp', 'w')
#out = sys.stdout

# command to be performed
cmd = "xquery doc('11mb')//item"
#cmd = "xquery 1";

cs = ClientSession.ClientSession('localhost', 1984, 'admin', 'admin')
cs.execute(cmd, out)
cs.close()

print t.timeit() * 1000, "ms"
