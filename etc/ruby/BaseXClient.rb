# -----------------------------------------------------------------------------
#
# This Ruby module provides methods to connect to and communicate with the
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

require 'socket'
require 'digest/md5'

# Constructor, creating a new socket connection.
class Session
  def initialize(host, port, username, pw)

    # create server connection
    @socket = TCPSocket.open(host, port)

    # receive timestamp
    ts = readString
    hash = Digest::MD5.new
    hash.update(hash.hexdigest(pw))
    hash.update(ts)

    # send username and hashed password/timestamp
    send(username)
    send(hash.hexdigest())

    # evaluate success flag
    if read != "\0"
      raise "Access denied."
    end

  end

  # Executes the specified command.
  def execute(com)
    # send command to server
    send(com)

    # receive result
    result = readString
    @info = readString
    if ok != true
      raise @info
    end
    return result
  end
  
  # Returns a query object.
  def query(cmd)
    return Query.new(self, cmd)
  end

  # Returns processing information.
  def info()
    return @info
  end

  # Closes the connection.
  def close()
    send("exit")
    @socket.close
  end

  # Receives a string from the socket.
  def readString()
    complete = ""
    while ((t = read) != "\0")
    complete += t
    end
    return complete
  end
  
  # Sends the defined str.
  def send(str)
    @socket.write(str + "\0")
  end

  # Returns a single byte from the socket.
  def read()
    return @socket.read(1)
  end
  
  # Returns success check. 
  def ok()
    return read == "\0"
  end
end

class Query
  # Constructor, creating a new query object.
  def initialize(s, q)
    @session = s
    @session.send("\0" + q)
    @id = @session.readString
    if @session.ok != true
      raise @session.readString
    end
  end
  
  # Checks for more parts of the result.  
  def more()
    @session.send("\1" + @id)
    @next = @session.readString
    if @session.ok != true
      raise @session.readString
    end
    return @next.length != 0
  end
  
  # Returns the next part of the result.
  def next()
    return @next
  end
  
  # Closes the iterative execution.
  def close()
    @session.send("\2" + @id)
  end
end
