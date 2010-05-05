# -----------------------------------------------------------------------------
#
# This Python module provides methods to connect to and communicate with the
# BaseX Server.
#
# The Constructor of the class expects a hostname, port, username and password
# for the connection. The socket connection will then be established via the
# hostname and the port.
#
# For the execution of commands you need to call the execute() method with the
# database command as argument. The method returns a boolean, indicating if
# the command was successful. The result can be requested with the result()
# method, and the info() method returns additional processing information
# or error output.
#
# -----------------------------------------------------------------------------
#
# Example:
#
# import BaseXClient
#
# try:
#   # create session
#   session = BaseXClient.Session('localhost', 1984, 'admin', 'admin')
#
#   # perform command and show result or error output
#   if session.execute("xquery 1 to 10"):
#     print session.result()
#   else:
#     print session.info()
#
#   # close session
#   session.close()
#
# except IOError as e:
#   # print exception
#   print e
#
# -----------------------------------------------------------------------------
# (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
# -----------------------------------------------------------------------------

import hashlib, socket, array

class Session():

  # Constructor, creating a new socket connection.
  def __init__(self, host, port, user, pw):
    # allocate buffer for speeding up communication
    self.__buf = array.array('B', '\0' * 0x1000)
    self.__init()

    # create server connection
    global s
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((host, port))

    # receive timestamp
    ts = self.__readString()

    # send username and hashed password/timestamp
    m = hashlib.md5()
    m.update(hashlib.md5(pw).hexdigest())
    m.update(ts)
    s.send(user + '\0' + m.hexdigest() + '\0')

    # evaluate success flag
    if s.recv(1) != '\0':
      raise IOError('Access Denied.')

  # Executes the specified command.
  def execute(self, com):
    # send command to server
    s.send(com + '\0')

    # send command to server and receive result
    self.__init()
    self.__result = self.__readString()
    self.__info = self.__readString()
    return self.__read() == 0

  # Returns the result.
  def result(self):
    return self.__result

  # Returns processing information.
  def info(self):
    return self.__info

  # Closes the connection.
  def close(self):
    s.send('exit\0')
    s.close()

  # Initializes the byte transfer.
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

  # Returns a single byte from the socket.
  def __read(self):
    # Cache next bytes
    if self.__bpos == self.__bsize:
      self.__bsize = s.recv_into(self.__buf)
      self.__bpos = 0
    b = self.__buf[self.__bpos]
    self.__bpos += 1
    return b
    
  # Sends command for iterate mode.  
  def send(self, query):
  	s.send('\0' + query + '\0')
  
  def read(self):
  	self.__init()
  	return self.__readString()
    
class Query():
	def __init__(self, session):
		self.__session = session
	
	def run(self, query):
		self.__session.send(query)
		return self.__session.read()
	
		
	
