require 'socket'
require 'digest/md5'

class Session
  def initialize(host, port, username, pw)
    @socket = TCPSocket.open(host, port)
    ts = readString
    hash = Digest::MD5.new
    hash.update(hash.hexdigest(pw))
    hash.update(ts)
    @socket.write(username + "\0")
    @socket.write(hash.hexdigest() + "\0")
    if read != "\0"
      raise "Access denied."
    end
    
  end
  
  def execute(com)
    @socket.write(com + "\0")
    @result = readString
    @info = readString
    return read
  end
  
  def result()
    return @result
  end
  
  def info()
    return @info
  end
  
  def close()
    @socket.write("exit \0")
    @socket.close
  end
  
  def readString()
    complete = ""
    while ((t = read) != "\0")
    complete += t
    end
    return complete
  end
  
  def read()
    return @socket.read(1)
  end

end
