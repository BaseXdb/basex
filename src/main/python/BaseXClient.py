"""
Python client for BaseX.
Works with BaseX 7.0 and later

Documentation: http://docs.basex.org/wiki/Clients

(C) BaseX Team 2005-12, Arjen van Elteren
    BSD License
"""

import hashlib, socket, array
import threading

class SocketInputReader(object):
    
    def __init__(self, sock):
        self.__s = sock
        self.__buf = array.array('B', chr(0) * 0x1000)
        self.init()
        
    def init(self):
        self.__bpos = 0
        self.__bsize = 0
        
    # Returns a single byte from the socket.
    def read(self):
        # Cache next bytes
        if self.__bpos >= self.__bsize:
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
        found = False
        substr = ""
        try:
            pos = self.__buf[self.__bpos:self.__bsize].index(byte)
            found = True
            substr = self.__buf[self.__bpos:pos+self.__bpos].tostring()
            self.__bpos = self.__bpos + pos + 1
        except ValueError:
            substr = self.__buf[self.__bpos:self.__bsize].tostring()
            self.__bpos = self.__bsize
        return (found, substr)

    def readString(self):
        strings = []
        found = False
        while not found:
            found, substr = self.read_until(0)
            strings.append(substr)
        return ''.join(strings)

class Session(object):
    # see readme.txt
    def __init__(self, host, port, user, pw):

        self.__info = None

        # create server connection
        self.__s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        
        self.__s.connect((host, port))
        
        self.__sreader = SocketInputReader(self.__s)
        
        self.__event_socket = None
        self.__event_host = host
        self.__event_listening_thread = None
        self.__event_callbacks = {}

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
    def create(self, name, content):
        self.sendInput(8, name, content)

    # see readme.txt
    def add(self, path, content):
        self.sendInput(9, path, content)

    # see readme.txt
    def replace(self, path, content):
        self.sendInput(12, path, content)

    # see readme.txt
    def store(self, path, content):
        self.sendInput(13, path, content)

    # see readme.txt
    def info(self):
        return self.__info

    # see readme.txt
    def close(self):
        self.send('exit')
        self.__s.close()
        if not self.__event_socket is None:
            self.__event_socket.close()

    def init(self):
        """Initialize byte transfer"""
        self.__sreader.init()
        
    def register_and_start_listener(self):
        self.__s.sendall(chr(10))
        event_port = int(self.readString())
        self.__event_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.__event_socket.settimeout(5000)
        self.__event_socket.connect((self.__event_host, event_port))
        token = self.readString()
        self.__event_socket.sendall(token + chr(0))
        if not self.__event_socket.recv(1) == chr(0):
            raise IOError("Could not register event listener")
        self.__event_listening_thread = threading.Thread(
            target=self.event_listening_loop
        )
        self.__event_listening_thread.daemon = True
        self.__event_listening_thread.start()
    
    def event_listening_loop(self):
        reader = SocketInputReader(self.__event_socket)
        reader.init()
        while True:
            name = reader.readString()
            data = reader.readString()
            self.__event_callbacks[name](data)
        
    def is_listening(self):
        return not self.__event_socket is None
        
    def watch(self, name, callback):
        if not self.is_listening():
            self.register_and_start_listener()
        else:
            self.__s.sendall(chr(10))
        self.send(name)
        info = self.readString()
        if not self.ok():
            raise IOError(info)
        self.__event_callbacks[name] = callback
        
    def unwatch(self, name):
        self.send(chr(11) + name)
        info = self.readString()
        if not self.ok():
            raise IOError(info)
        del self.__event_callbacks[name]
            
    def readString(self):
        """Retrieve a string from the socket"""
        return self.__sreader.readString()

    def read(self):
        """Return a single byte from socket"""
        return self.__sreader.read()

    def read_until(self, byte):
        """Read until byte is found"""
        return self.__sreader.read_until(byte)

    def send(self, value):
        """Send the defined string"""
        self.__s.sendall(value + chr(0))

    # see readme.txt
    def sendInput(self, code, arg, content):
        self.__s.sendall(chr(code) + arg + chr(0) + content + chr(0))
        self.__info = self.readString()
        if not self.ok():
            raise IOError(self.info())

    def ok(self):
        """Return success check"""
        return self.read() == 0

    def receive(self):
        """Return received string"""
        self.init()
        return self.readString()
    
    def iter_receive(self):
        self.init()
        typecode = self.read()
        while typecode > 0:
            string = self.readString()
            yield string
            typecode = self.read()
        if not self.ok():
            raise IOError(self.readString())
            

class Query():
    # see readme.txt
    def __init__(self, session, q):
        self.__session = session
        self.__id = self.exc(chr(0), q)

    # see readme.txt  
    def bind(self, name, value, datatype=''):
        self.exc(chr(3), self.__id + chr(0) + name + chr(0) + value + chr(0) + datatype)

    # see readme.txt  
    def context(self, value, datatype=''):
        self.exc(chr(14), self.__id + chr(0) + value + chr(0) + datatype)
        
    # see readme.txt  
    def iter(self):
        self.__session.send(chr(4) + self.__id)
        return self.__session.iter_receive()

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
