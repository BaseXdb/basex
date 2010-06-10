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
# database command as argument. The method returns the result or throws
# an exception with the received error message.
# For the execution of the iterative version of a query you need to call
# the query() method. The results will then be returned via the more() and
# the next() methods. If an error occurs an exception will be thrown.
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
    self.init()

    # create server connection
    global s
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((host, port))

    # receive timestamp
    ts = self.readString()

    # send username and hashed password/timestamp
    m = hashlib.md5()
    m.update(hashlib.md5(pw).hexdigest())
    m.update(ts)
    self.send(user + '\0' + m.hexdigest())

    # evaluate success flag
    if s.recv(1) != '\0':
      raise IOError('Access Denied.')

  # Executes the specified command.
  def execute(self, com):
    # send command to server
    self.send(com)

    # receive result
    result = self.receive()
    self.__info = self.readString()
    if not self.ok():
      raise IOError(self.__info)
    return result
  
  # Returns a query object.  
  def query(self, q):
  	return Query(self, q)

  # Returns processing information.
  def info(self):
    return self.__info

  # Closes the connection.
  def close(self):
    self.send('exit')
    s.close()

  # Initializes the byte transfer.
  def init(self):
    self.__bpos = 0
    self.__bsize = 0

  # Receives a string from the socket.
  def readString(self):
    bf = array.array('B')
    while True:
      b = self.read()
      if b:
        bf.append(b)
      else:
        return bf.tostring()

  # Returns a single byte from the socket.
  def read(self):
    # Cache next bytes
    if self.__bpos == self.__bsize:
      self.__bsize = s.recv_into(self.__buf)
      self.__bpos = 0
    b = self.__buf[self.__bpos]
    self.__bpos += 1
    return b
    
  # Sends the defined str.	
  def send(self, str):
  	s.send(str + '\0')
  
  # Returns success check.	
  def ok(self):
   	return self.read() == 0
   
  # Returns the received string. 	
  def receive(self):
  	self.init()
  	return self.readString()
     
   
class Query():
	# Constructor, creating a new query object.
	def __init__(self, session, q):
	  self.__session = session
	  self.__session.send('\0' + q)
	  self.__id = self.__session.receive()
	  if not self.__session.ok():
	    raise IOError(self.__session.readString())
	
	# Checks for more parts of the result.
	def more(self):
	  self.__session.send('\1' + self.__id)
	  self.__next = self.__session.receive()
	  if not self.__session.ok():
	    raise IOError(self.__session.readString())	
	  return len(self.__next) != 0	
		
	# Returns the next part of the result.
	def next(self):
	  return self.__next
	
	# Closes the iterative execution.	
	def close(self):
	  self.__session.send('\2' + self.__id);