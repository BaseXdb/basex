# -*- coding: utf-8 -*-
"""
Python 2.7.3 and 3.x client for BaseX.
Works with BaseX 7.0 and later

Requires Python 3.x or Python 2.x having some backports like bytearray.
(I've tested Python 3.2.3, and Python 2.7.3 on Fedora 16 linux x86_64.)

LIMITATIONS:

* binary content would corrupt, maybe. (I didn't test it)
* also, will fail to extract stored binary content, maybe.
  (both my code, and original don't care escaped 0xff.)

Documentation: http://docs.basex.org/wiki/Clients

(C) 2012, Hiroaki Itoh. BSD License
    updated 2014 by Marc van Grootel

"""

import hashlib, socket
import threading

# ---------------------------------
#
class SocketWrapper(object):
    """a wrapper to python native socket module."""

    def __init__(self, sock,
                 receive_bytes_encoding='utf-8',
                 send_bytes_encoding='utf-8'):

        self.receive_bytes_encoding = receive_bytes_encoding
        self.send_bytes_encoding = send_bytes_encoding

        self.terminator = bytearray(chr(0), self.receive_bytes_encoding)
        self.__s = sock
        self.__buf = bytearray(chr(0) * 0x1000, self.receive_bytes_encoding)
        self.__bpos = 0
        self.__bsize = 0

    def clear_buffer(self):
        """reset buffer status for next invocation ``recv_until_terminator()``
or ``recv_single_byte()``."""
        self.__bpos = 0
        self.__bsize = 0

    def __fill_buffer(self):
        """cache next bytes"""
        if self.__bpos >= self.__bsize:
            self.__bsize = self.__s.recv_into(self.__buf)
            self.__bpos = 0

    # Returns a single byte from the socket.
    def recv_single_byte(self):
        """recv a single byte from previously fetched buffer."""
        self.__fill_buffer()
        result_byte = self.__buf[self.__bpos]
        self.__bpos += 1
        return result_byte

    # Reads until terminator byte is found.
    def recv_until_terminator(self):
        """recv a nul(or specified as terminator_byte)-terminated whole string
from previously fetched buffer."""
        result_bytes = bytearray()
        while True:
            self.__fill_buffer()
            pos = self.__buf.find(self.terminator, self.__bpos, self.__bsize)
            if pos >= 0:
                result_bytes.extend(self.__buf[self.__bpos:pos])
                self.__bpos = pos + 1
                break
            else:
                result_bytes.extend(self.__buf[self.__bpos:self.__bsize])
                self.__bpos = self.__bsize
        return result_bytes.decode(self.receive_bytes_encoding)

    def sendall(self, data):
        """sendall with specified byte encoding if data is not bytearray, bytes
(maybe str). if data is bytearray or bytes, it will be passed to native sendall API
directly."""
        if isinstance(data, (bytearray, bytes)):
            return self.__s.sendall(data)
        return self.__s.sendall(bytearray(data, self.send_bytes_encoding))

    def __getattr__(self, name):
        return lambda *arg, **kw: getattr(self.__s, name)(*arg, **kw)


# ---------------------------------
#
class Session(object):
    """class Session.

    see http://docs.basex.org/wiki/Server_Protocol
    """

    def __init__(self, host, port, user, password,
                 receive_bytes_encoding='utf-8',
                 send_bytes_encoding='utf-8'):
        """Create and return session with host, port, user name and password"""

        self.__info = None

        # create server connection
        self.__swrapper = SocketWrapper(
            socket.socket(socket.AF_INET, socket.SOCK_STREAM),
            receive_bytes_encoding=receive_bytes_encoding,
            send_bytes_encoding=send_bytes_encoding)

        self.__swrapper.connect((host, port))

        self.__event_socket_wrapper = None
        self.__event_host = host
        self.__event_listening_thread = None
        self.__event_callbacks = {}

        # receive timestamp
        response = self.recv_c_str().split(':')

        # send username and hashed password/timestamp
        hfun = hashlib.md5()

        if len(response) > 1:
            code = "%s:%s:%s" % (user,response[0],password)
            nonce = response[1]
        else:
            code = password
            nonce = response[0]

        hfun.update(hashlib.md5(code.encode('us-ascii')).hexdigest().encode('us-ascii'))
        hfun.update(nonce.encode('us-ascii'))
        self.send(user + chr(0) + hfun.hexdigest())

        # evaluate success flag
        if not self.server_response_success():
            raise IOError('Access Denied.')

    def execute(self, com):
        """Execute a command and return the result"""
        # send command to server
        self.send(com)

        # receive result
        result = self.receive()
        self.__info = self.recv_c_str()
        if not self.server_response_success():
            raise IOError(self.__info)
        return result

    def query(self, querytxt):
        """Creates a new query instance (having id returned from server)."""
        return Query(self, querytxt)

    def create(self, name, content):
        """Creates a new database with the specified input (may be empty)."""
        self.__send_input(8, name, content)

    def add(self, path, content):
        """Adds a new resource to the opened database."""
        self.__send_input(9, path, content)

    def replace(self, path, content):
        """Replaces a resource with the specified input."""
        self.__send_input(12, path, content)

    def store(self, path, content):
        """Stores a binary resource in the opened database.

api won't escape 0x00, 0xff automatically, so you must do it
yourself explicitly."""
        # ------------------------------------------
        # chr(13) + path + chr(0) + content + chr(0)
        self.__send_binary_input(13, path, content)
        #
        # ------------------------------------------

    def info(self):
        """Return process information"""
        return self.__info

    def close(self):
        """Close the session"""
        self.send('exit')
        self.__swrapper.close()
        if not self.__event_socket_wrapper is None:
            self.__event_socket_wrapper.close()

    def __register_and_start_listener(self,
                                      receive_bytes_encoding=None,
                                      send_bytes_encoding=None):
        """register and start listener."""

        if receive_bytes_encoding:
            receive_bytes_encoding_ = receive_bytes_encoding
        else:
            receive_bytes_encoding_ = self.__swrapper.receive_bytes_encoding
        if send_bytes_encoding:
            send_bytes_encoding_ = send_bytes_encoding
        else:
            send_bytes_encoding_ = self.__swrapper.send_bytes_encoding

        self.__swrapper.sendall(chr(10))
        event_port = int(self.recv_c_str())
        self.__event_socket_wrapper = SocketWrapper(
            socket.socket(socket.AF_INET, socket.SOCK_STREAM),
            receive_bytes_encoding=receive_bytes_encoding_,
            send_bytes_encoding=send_bytes_encoding_)
        self.__event_socket_wrapper.settimeout(5000)
        self.__event_socket_wrapper.connect((self.__event_host, event_port))
        token = self.recv_c_str()
        self.__event_socket_wrapper.sendall(token + chr(0))
        #if self.__event_socket_wrapper.recv_single_byte() != chr(0):
        #    raise IOError("Could not register event listener")
        #java example client does skip next byte...
        ign = self.__event_socket_wrapper.recv_single_byte()

        self.__event_listening_thread = threading.Thread(
            target=self.__event_listening_loop
        )
        self.__event_listening_thread.daemon = True
        self.__event_listening_thread.start()

    def __event_listening_loop(self):
        """event listening loop (in subthread)"""
        reader = self.__event_socket_wrapper
        reader.clear_buffer()
        while True:
            name = reader.recv_until_terminator()
            data = reader.recv_until_terminator()
            self.__event_callbacks[name](data)

    def is_listening(self):
        """true if registered and started listener, false otherwise"""
        return not self.__event_socket_wrapper is None

    def watch(self, name, callback):
        """Watch the specified event"""
        if not self.is_listening():
            self.__register_and_start_listener()
        else:
            self.__swrapper.sendall(chr(10))
        self.send(name)
        info = self.recv_c_str()
        if not self.server_response_success():
            raise IOError(info)
        self.__event_callbacks[name] = callback

    def unwatch(self, name):
        """Unwatch the specified event"""
        self.send(chr(11) + name)
        info = self.recv_c_str()
        if not self.server_response_success():
            raise IOError(info)
        del self.__event_callbacks[name]

    def recv_c_str(self):
        """Retrieve a string from the socket"""
        return self.__swrapper.recv_until_terminator()

    def send(self, value):
        """Send the defined string"""
        self.__swrapper.sendall(value + chr(0))

    def __send_input(self, code, arg, content):
        """internal. don't care."""
        self.__swrapper.sendall(chr(code) + arg + chr(0) + content + chr(0))
        self.__info = self.recv_c_str()
        if not self.server_response_success():
            raise IOError(self.info())

    def __send_binary_input(self, code, path, content):
        """internal. don't care."""
        #at this time, we can't use __send_input itself because of encoding
        #problem. we have to build bytearray directly.
        if not isinstance(content, (bytearray, bytes)):
            raise ValueError("Sorry, content must be bytearray or bytes, not " +
                             str(type(content)))

        # ------------------------------------------
        # chr(code) + path + chr(0) + content + chr(0)
        data = bytearray([code])
        try:
            data.extend(path)
        except:
            data.extend(path.encode('utf-8'))
        data.extend([0])
        data.extend(content)
        data.extend([0])
        #
        # ------------------------------------------
        self.__swrapper.sendall(data)
        self.__info = self.recv_c_str()
        if not self.server_response_success():
            raise IOError(self.info())

    def server_response_success(self):
        """Return success check"""
        return self.__swrapper.recv_single_byte() == 0

    def receive(self):
        """Return received string"""
        self.__swrapper.clear_buffer()
        return self.recv_c_str()

    def iter_receive(self):
        """iter_receive() -> (typecode, item)

iterate while the query returns items.
typecode list is in http://docs.basex.org/wiki/Server_Protocol:_Types
"""
        self.__swrapper.clear_buffer()
        typecode = self.__swrapper.recv_single_byte()
        while typecode > 0:
            string = self.recv_c_str()
            yield (typecode, string)
            typecode = self.__swrapper.recv_single_byte()
        if not self.server_response_success():
            raise IOError(self.recv_c_str())

# ---------------------------------
#
class Query():
    """class Query.

    see http://docs.basex.org/wiki/Server_Protocol
    """

    def __init__(self, session, querytxt):
        """Create query object with session and query"""
        self.__session = session
        self.__id = self.__exc(chr(0), querytxt)

    def bind(self, name, value, datatype=''):
        """Binds a value to a variable.
An empty string can be specified as data type."""
        self.__exc(chr(3), self.__id + chr(0) + name + chr(0) + value + chr(0) + datatype)

    def context(self, value, datatype=''):
        """Bind the context item"""
        self.__exc(chr(14), self.__id + chr(0) + value + chr(0) + datatype)

    def iter(self):
        """iterate while the query returns items"""
        self.__session.send(chr(4) + self.__id)
        return self.__session.iter_receive()

    def execute(self):
        """Execute the query and return the result"""
        return self.__exc(chr(5), self.__id)

    def info(self):
        """Return query information"""
        return self.__exc(chr(6), self.__id)

    def options(self):
        """Return serialization parameters"""
        return self.__exc(chr(7), self.__id)

    def updating(self):
        """Returns true if the query may perform updates; false otherwise."""
        return self.__exc(chr(30), self.__id)

    def full(self):
        """Returns all resulting items as strings, prefixed by XDM Meta Data."""
        return self.__exc(chr(31), self.__id)

    def close(self):
        """Close the query"""
        self.__exc(chr(2), self.__id)

    def __exc(self, cmd, arg):
        """internal. don't care."""
        #should we expose this?
        #(this makes sense only when mismatch between C/S is existing.)
        self.__session.send(cmd + arg)
        result = self.__session.receive()
        if not self.__session.server_response_success():
            raise IOError(self.__session.recv_c_str())
        return result
