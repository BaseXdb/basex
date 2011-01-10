# Language Binding for BaseX.
# Works with BaseX 6.3.1 and later
# Documentation: http://basex.org/api
#
# (C) BaseX Team 2005-11, ISC License

import hashlib, socket, array

class Session():

  # see readme.txt
  def __init__(self, host, port, user, pw):
    # allocate buffer for speeding up communication
    self.__buf = array.array('B', '\0' * 0x1000)
    self.init()

    # create server connection
    global s
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((host, port))

    # receive timestamp
    ts = self.receive()

    # send username and hashed password/timestamp
    m = hashlib.md5()
    m.update(hashlib.md5(pw).hexdigest())
    m.update(ts)
    self.send(user + '\0' + m.hexdigest())

    # evaluate success flag
    if s.recv(1) != '\0':
      raise IOError('Access Denied.')

  # see readme.txt
  def execute(self, com):
    # send command to server
    self.send(com)

    # receive result
    result = self.receive()
    self.__info = self.receive()
    if not self.ok():
      raise IOError(self.__info)
    return result
  
  # see readme.txt
  def query(self, q):
    return Query(self, q)

  # see readme.txt
  def info(self):
    return self.__info

  # see readme.txt
  def close(self):
    self.send('exit')
    s.close()

  # Initializes the byte transfer.
  def init(self):
    self.__bpos = 0
    self.__bsize = 0

  # Receives a string from the socket.
  def receive(self):
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
    return self.receive()
     
   
class Query():
  # see readme.txt
  def __init__(self, session, q):
    self.__session = session
    self.__id = self.execu('\0', q)
  
  # see readme.txt  
  def init(self):
    return self.execu('\4', self.__id)
    
  # see readme.txt  
  def bind(self, name, value):
    self.execu('\3', self.__id + '\0' + name + '\0' + value + '\0')
  
  # see readme.txt
  def more(self):
    self.__next = self.execu('\1', self.__id)  
    return len(self.__next) != 0  
    
  # see readme.txt
  def next(self):
    return self.__next
  
  # see readme.txt  
  def execute(self):
    return self.execu('\5', self.__id)
  
  # see readme.txt  
  def info(self):
    return self.execu('\6', self.__id)
  
  # see readme.txt  
  def close(self):
    return self.execu('\2', self.__id)
  
  # see readme.txt  
  def execu(self, cmd, arg):
    self.__session.send(cmd + arg)
    s = self.__session.receive()
    if not self.__session.ok():
      raise IOError(self.__session.receive())
    return s
