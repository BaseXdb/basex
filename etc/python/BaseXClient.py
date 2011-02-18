# Language Binding for BaseX.
# Works with BaseX 6.3.1 and later
# Documentation: http://basex.org/api
#
# (C) BaseX Team 2005-11, ISC License

import hashlib, socket, array
import string

class Session():

    # see readme.txt
    def __init__(self, host, port, user, pw):
        # allocate buffer for speeding up communication
        self.__buf = array.array('B', chr(0) * 0x1000)
        self.init()
        
        # create server connection
        self.__s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.__s.connect((host, port))
        
        # receive timestamp
        ts = self.readString()
        
        # send username and hashed password/timestamp
        m = hashlib.md5()
        m.update(hashlib.md5(pw).hexdigest())
        m.update(ts)
        self.send(user + chr(0) + m.hexdigest())
        
        # evaluate success flag
        if self.__s.recv(1) != chr(0):
            raise IOError('Access Denied.')

    # see readme.txt
    def execute(self, com):
        # send command to server
        self.send(com)
        
        # receive result
        result = self.receive()
        self.__info = self.readString()
        if not self.ok():
            raise IOError(self.__info)
        return result
  
    # see readme.txt
    def query(self, q):
        return Query(self, q)
    
    # see readme.txt
    def add(self, name, target, content):
        self.__s.send(chr(9) + name + chr(0) + target + chr(0) + content + chr(0))
        self.__info = self.readString()
        if not self.ok():
            raise IOError(self.info())

    # see readme.txt
    def info(self):
        return self.__info

    # see readme.txt
    def close(self):
        self.send('exit')
        self.__s.close()

    # Initializes the byte transfer.
    def init(self):
        self.__bpos = 0
        self.__bsize = 0

    # Receives a string from the socket.        
    def readString(self):
        strings = []
        found = False
        while not found:
            found, substr = self.read_until(0)
            strings.append(substr)
        return string.join(strings, "")

    # Returns a single byte from the socket.
    def read(self):
        # Cache next bytes
        if self.__bpos == self.__bsize:
            self.__bsize = self.__s.recv_into(self.__buf)
            self.__bpos = 0
        b = self.__buf[self.__bpos]
        self.__bpos += 1
        return b
    
    # Reads until byte is found.
    def read_until(self, byte):
        # Cache next bytes
        if self.__bpos >= self.__bsize:
            self.__bsize = self.__s.recv_into(self.__buf)
            self.__bpos = 0
            #print self.__buf[:100]
        found = False
        substr = ""
        try:
            pos = self.__buf[self.__bpos:self.__bsize].index(byte)
            found = True
            substr = self.__buf[self.__bpos:pos].tostring()
            self.__bpos = self.__bpos + pos + 1
        except ValueError:
            substr = self.__buf[self.__bpos:self.__bsize].tostring()
            self.__bpos = self.__bsize
        return (found, substr)
    
    # Sends the defined str.
    def send(self, str):
        self.__s.send(str + chr(0))
  
    # Returns success check.
    def ok(self):
        return self.read() == 0
   
    # Returns the received string.
    def receive(self):
        self.init()
        return self.readString()
   
class Query():
    # see readme.txt
    def __init__(self, session, q):
        self.__session = session
        self.__id = self.execu(chr(0), q)
  
    # see readme.txt  
    def init(self):
        return self.execu(chr(4), self.__id)
    
    # see readme.txt  
    def bind(self, name, value):
        self.execu(chr(3), self.__id + chr(0) + name + chr(0) + value + chr(0))
  
    # see readme.txt
    def more(self):
        self.__next = self.execu(chr(1), self.__id)  
        return len(self.__next) != 0  
    
    # see readme.txt
    def next(self):
        return self.__next
  
    # see readme.txt  
    def execute(self):
        return self.execu(chr(5), self.__id)
  
    # see readme.txt  
    def info(self):
        return self.execu(chr(6), self.__id)
  
    # see readme.txt  
    def close(self):
        return self.execu(chr(2), self.__id)
  
    # see readme.txt  
    def execu(self, cmd, arg):
        self.__session.send(cmd + arg)
        s = self.__session.receive()
        if not self.__session.ok():
            raise IOError(self.__session.readString())
        return s