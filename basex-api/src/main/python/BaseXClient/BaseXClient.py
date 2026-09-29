# -*- coding: utf-8 -*-
"""
Python 3 client for BaseX.
Works with BaseX 13.0 and later

Documentation: https://docs.basex.org/wiki/Clients

(C) 2012, Hiroaki Itoh. BSD License
    updated 2014 by Marc van Grootel

"""

import hashlib
import socket

# ---------------------------------
#


class SocketWrapper:
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
        """recv a nul-terminated whole string from previously fetched buffer.
0x00 and 0xFF bytes prefixed with 0xFF are unescaped."""
        result_bytes = bytearray()
        while True:
            self.__fill_buffer()
            start, end = self.__bpos, self.__bsize
            # find next 0x00 or 0xFF byte, copy all bytes before it
            pos = min((p for p in (self.__buf.find(0, start, end),
                                   self.__buf.find(0xFF, start, end)) if p >= 0), default=end)
            result_bytes.extend(self.__buf[start:pos])
            self.__bpos = pos
            if pos == end:
                continue
            self.__bpos += 1
            if self.__buf[pos] == 0:
                return result_bytes
            result_bytes.append(self.recv_single_byte())

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
class Session:
    """class Session.

    see https://docs.basex.org/wiki/Server_Protocol
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

        # receive challenge: {realm}:{nonce}
        nonce = self.recv_c_str().split(':')[1]

        # request the password parameters: send username and an empty hash
        self.send(user)
        self.send('')

        # receive password parameters: {algorithm}:{salt}
        salt = self.recv_c_str().split(':')[1]

        # send hashed password
        code = hashlib.sha256((salt + password).encode('us-ascii')).hexdigest()
        hfun = hashlib.sha256()
        hfun.update(code.encode('us-ascii'))
        hfun.update(nonce.encode('us-ascii'))
        self.send(hfun.hexdigest())

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

    def put(self, path, content):
        """Puts (adds or replaces) a document in the opened database."""
        self.__send_input(12, path, content)

    def put_binary(self, path, content):
        """Puts (adds or replaces) a binary resource in the opened database.
The content must be of type bytes or bytearray."""
        if not isinstance(content, (bytearray, bytes)):
            raise ValueError("Content must be bytearray or bytes, not " + str(type(content)))
        self.__send_input(13, path, content)

    def info(self):
        """Return process information"""
        return self.__info

    def close(self):
        """Close the session"""
        self.send('exit')
        self.__swrapper.close()

    def recv_c_str(self):
        """Retrieve a string from the socket"""
        return self.__swrapper.recv_until_terminator().decode(self.__swrapper.receive_bytes_encoding)

    def send(self, value):
        """Send the defined string"""
        self.__swrapper.sendall(value + chr(0))

    def __send_input(self, code, arg, content):
        """Sends a command with an argument and input: 0x00 and 0xFF bytes are prefixed with 0xFF."""
        encoding = self.__swrapper.send_bytes_encoding
        if not isinstance(content, (bytearray, bytes)):
            content = content.encode(encoding)
        content = bytes(content).replace(b'\xff', b'\xff\xff').replace(b'\x00', b'\xff\x00')
        self.__swrapper.sendall(bytes([code]) + arg.encode(encoding) + b'\x00' + content + b'\x00')
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
        """Iterates over the items returned by a query, and yields them as strings.
The type codes (see https://docs.basex.org/main/Server_Protocol#type_ids) are skipped."""
        result = list()
        self.__swrapper.clear_buffer()
        typecode = self.__swrapper.recv_single_byte()
        while typecode > 0:
            result.append(self.__swrapper.recv_until_terminator())
            typecode = self.__swrapper.recv_single_byte()
        if not self.server_response_success():
            raise IOError(self.recv_c_str())
        for ba in result:
            yield ba.decode(self.__swrapper.receive_bytes_encoding)


# ---------------------------------
#


class Query:
    """class Query.

    see https://docs.basex.org/wiki/Server_Protocol
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
        """Bind the context value"""
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
        # should we expose this?
        # (this makes sense only when mismatch between C/S is existing.)
        self.__session.send(cmd + arg)
        result = self.__session.receive()
        if not self.__session.server_response_success():
            raise IOError(self.__session.recv_c_str())
        return result
