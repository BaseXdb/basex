# Python client for BaseX.
# Works with BaseX 7.0 and later
#
# Documentation: http://docs.basex.org/wiki/Clients
#
# (C) BaseX Team 2005-11, BSD License

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
    def create(self, name, input):
        self.sendInput(8, name, input)

    # see readme.txt
    def add(self, path, input):
        self.sendInput(9, path, input)

    # see readme.txt
    def replace(self, path, input):
        self.sendInput(12, path, input)

    # see readme.txt
    def store(self, path, input):
        self.sendInput(13, path, input)

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

    # see readme.txt
    def sendInput(self, code, arg, input):
        self.__s.send(chr(code) + arg + chr(0) + input + chr(0))
        self.__info = self.readString()
        if not self.ok():
            raise IOError(self.info())

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
        self.__id = self.exc(chr(0), q)

    # see readme.txt  
    def bind(self, name, value):
        self.exc(chr(3), self.__id + chr(0) + name + chr(0) + value + chr(0))

    # see readme.txt  
    def execute(self):
        return self.exc(chr(5), self.__id)

    # see readme.txt  
    def info(self):
        return self.exc(chr(6), self.__id)

    # see readme.txt  
    def options(self):
        return self.exc(chr(7), self.__id)

    # see readme.txt  
    def close(self):
        self.exc(chr(2), self.__id)

    # see readme.txt  
    def exc(self, cmd, arg):
        self.__session.send(cmd + arg)
        s = self.__session.receive()
        if not self.__session.ok():
            raise IOError(self.__session.readString())
        return s