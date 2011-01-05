# Language Binding for BaseX.
# Works with BaseX 6.3.1 and later
# Documentation: http://basex.org/api
#
# (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License

require 'socket'
require 'digest/md5'

# see readme.txt
class Session
  def initialize(host, port, username, pw)

    # create server connection
    @socket = TCPSocket.open(host, port)

    # receive timestamp
    ts = receive
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

  # see readme.txt
  def execute(com)
    # send command to server
    send(com)

    # receive result
    result = receive
    @info = receive
    if ok != true
      raise @info
    end
    return result
  end
  
  # see readme.txt
  def query(cmd)
    return Query.new(self, cmd)
  end

  # see readme.txt
  def info()
    return @info
  end

  # see readme.txt
  def close()
    send("exit")
    @socket.close
  end

  # Receives a string from the socket.
  def receive()
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
  # see readme.txt
  def initialize(s, q)
    @session = s
    @id = exec("\0", q)
  end
  
  # see readme.txt
  def init()
    return exec("\4", @id)
  end
  
  # see readme.txt
  def bind(name, value)
    exec("\3", @id + "\0" + name + "\0" + value + "\0")
  end
  
  # see readme.txt  
  def more()
    @next = exec("\1", @id)
    return @next.length != 0
  end
  
  # see readme.txt
  def next()
    return @next
  end
  
  # see readme.txt
  def execute()
    return exec("\5", @id)
  end
  
  # see readme.txt
  def info()
    return exec("\6", @id)
  end
  
  # see readme.txt
  def close()
    return exec("\2", @id)
  end
  
  # see readme.txt
  def exec(cmd, arg)
    @session.send(cmd + arg)
    s = @session.receive
    if @session.ok != true
      raise @session.receive
    end
    return s
  end
end
