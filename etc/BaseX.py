# This Python module all methods to connect and communicate with the
# BaseX Server.
#
# The Session class manages the communication between server and client.
# This class has to be called by your client code (see Example.py).
#
# The Constructor of the Session class expects a hostname, port, username and
# password for the connection. The socket connection will then be established
# via the hostname and the port.
#
# The Client class is a console client which allows to interactively input
# commands.
#
# For the execution of commands you need to call the execute method with the command
# as argument. The result and the info will then be written to the corresponding string.
# These strings can be fetched with the methods result() and info().
#
# Example:
# 
# import BaseX
# try:
# # create session
# cs = BaseX.Session('localhost', 1984, 'admin', 'admin')
# # perform command; show info if something went wrong
# if not cs.execute("xquery 1 + 2):
#    print cs.info()
#  else:
#    print cs.result()
# # close session
# cs.close()
# except IOError as e:
#  print e
#
# (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License

import hashlib, socket, array, getopt, sys, getpass

class Session():

  # Constructor.
  def __init__(self, host, port, user, pw):
    # allocate 4kb buffer
    self.__buf = array.array('B', '\0' * 0x1000)
    self.__init()

    # create server connection
    global s
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((host, port))

    # receive timestamp
    ts = self.__readString()
    
    # code password and timestamp in md5
    # send user name and hashed password/timestamp
    m = hashlib.md5()
    m.update(hashlib.md5(pw).hexdigest())
    m.update(ts)

    s.send(user + '\0')
    s.send(m.hexdigest() + '\0')

    # receives success flag
    if s.recv(1) != '\0':
      raise IOError('Access Denied.')

  # Executes a command.
  def execute(self, com):
    # send command to server
    s.send(com + '\0')

    # receive result
    self.__init()
    self.__result = self.__readString()
    self.__info = self.__readString()
    return self.__read() == 0

  # Returns the result.
  def result(self):
    return self.__result

  # Returns the info string.
  def info(self):
    return self.__info

  # Closes the socket.
  def close(self):
    s.send('exit \0')
    s.close()

  # Initializes the byte transfer
  def __init(self):
    self.__bpos = 0
    self.__bsize = 0

  # Receives a string from the socket.
  def __readString(self):
    bf = array.array('B')
    while True:
      b = self.__read()
      if b:
        bf.append(b)
      else:
        return bf.tostring()

  # Returns the next byte.
  def __read(self):
    # Cache next bytes
    if self.__bpos == self.__bsize:
      self.__bsize = s.recv_into(self.__buf)
      self.__bpos = 0

    # Return current byte
    b = self.__buf[self.__bpos]
    self.__bpos += 1
    return b


# This class offers an interactive BaseX console.
class Client(object):
  # Initializes the client.
  def __init__(self, host, port):
    self.__host = host
    self.__port = port
    print 'BaseX Client'
    print 'Try "help" to get some information.'

  # Creates a session.
  def session(self):
    user = raw_input('Username: ');
    pw = getpass.getpass('Password: ');
    try:
      global session
      session = Session(self.__host, self.__port, user, pw)
      self.__console()
      session.close()
      print 'See you.'
    except IOError as e:
      print e

  # Runs the console.
  def __console(self):
    session.execute('SET INFO ON')
    while True:
      com = str(raw_input('> ')).strip()
      if com == 'exit':
        break
      if com:
        session.execute(com)
        print session.info()

# Reads arguments -p and -h.
def opts():
    try:
      opts, args = getopt.getopt(sys.argv[1:], '-p:-h', ['port', 'host'])
    except getopt.GetoptError, err:
      print str(err)
      sys.exit()
    global host
    global port
    host = 'localhost'
    port = 1984

    for o, a in opts:
      if o == '-p':
        port = int(a)
      if o == '-h':
        host = a

# Main method.
if __name__ == '__main__':
  opts()
  Client(host, port).session()
