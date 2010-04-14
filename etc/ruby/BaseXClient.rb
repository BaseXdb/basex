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
# database command as argument. The method returns a boolean, indicating if
# the command was successful. The result can be requested with the result()
# method, and the info() method returns additional processing information
# or error output.
#
# -----------------------------------------------------------------------------
#
# Example:
#
# require 'BaseXClient.rb'
#
# begin
# session = Session.new("localhost", 1984, "admin", "admin")
# if session.execute("xquery 1 to 10") == "\0"
#   puts session.result
# else
#  puts session.info
# end
# session.close
# rescue Exception => e
#   puts e
# end
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
    @socket.write(username + "\0")
    @socket.write(hash.hexdigest() + "\0")

    # evaluate success flag
    if read != "\0"
      raise "Access denied."
    end

  end

  # Executes the specified command.
  def execute(com)
    # send command to server
    @socket.write(com + "\0")

    # send command to server and receive result
    @result = readString
    @info = readString
    return read
  end

  # Returns the result.
  def result()
    return @result
  end

  # Returns processing information.
  def info()
    return @info
  end

  # Closes the connection.
  def close()
    @socket.write("exit \0")
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

  # Returns a single byte from the socket.
  def read()
    return @socket.read(1)
  end
end
