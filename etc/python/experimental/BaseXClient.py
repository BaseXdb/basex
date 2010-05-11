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
    s.send(user + '\0' + m.hexdigest() + '\0')

    # evaluate success flag
    if s.recv(1) != '\0':
      raise IOError('Access Denied.')

  # Executes the specified command.
  def execute(self, com):
    # send command to server
    s.send(com + '\0')

    # send command to server and receive result
    self.init()
    self.__result = self.readString()
    self.__info = self.readString()
    return self.read() == 0
  
  # Returns a query object.  
  def query(self, q):
  	return Query(self, q)

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
  
  # Executes the iterative mode of a query.
  def executeIter(self, com):
  	s.send('\0' + com + '\0');
  	return self.check()
  
  # Sends the defined sign.	
  def send(self, sign):
  	s.send(sign)
  
  # Checks the next byte for null or 1.	
  def check(self):
   	return s.recv(1) == '\0'
   
  # Returns the received string. 	
  def res(self):
  	self.init()
  	return self.readString()
     
   
class Query():
	# Constructor, creating a new query object.
	def __init__(self, session, q):
		self.__session = session
		self.__query = q
	
	# Runs the query and returns the success flag.
	def run(self):
		return self.__session.executeIter(self.__query)
	
	# Checks for more parts of the result.	
	def more(self):
		self.__session.send('\0')
		if self.__session.check():
			self.__part = self.__session.res()
			return True
		else:
			self.close()
			return False
	
	# Returns the next part of the result.
	def next(self):
		return self.__part		
	
	# Closes the iterative execution.	
	def close(self):
		self.__session.send('\1')
	
	# Returns the error info.	
	def info(self):
		return self.__session.res()