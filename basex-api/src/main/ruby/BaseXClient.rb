# Ruby client for BaseX.
# Works with BaseX 7.x (but not with BaseX 8.0 and later)
#
# Documentation: http://docs.basex.org/wiki/Clients
#
# (C) BaseX Team 2005-12, BSD License

require 'socket'
require 'digest/md5'

module BaseXClient
  class Session
    def initialize(host, port, username, pw)

      # create server connection
      @socket = TCPSocket.open(host, port)
      
      # authenticate
      rec = receive.split(':')
      realm = rec[0]
      nonce = rec[1]
      hash = Digest::MD5.new
      hash.update(hash.hexdigest([username, realm, pw].join(':')))
      hash.update(nonce)
      send(username)
      send(hash.hexdigest())

      # evaluate success flag
      if read != 0.chr
        raise "Access denied."
      end

      @char_lead_byte = "\xFF"
      @char_lead_byte.force_encoding('ASCII-8BIT')
    end

    def execute(com)
      # send command to server
      send(com)

      # receive result
      result = receive
      @info = receive
      if !ok
        raise @info
      end
      return result
    end
    
    def query(cmd)
      return Query.new(self, cmd)
    end
    
    def create(name, input)
      sendCmd(8.chr, name, input)
    end
    
    def add(path, input)
      sendCmd(9.chr, path, input)
    end

    def replace(path, input)
      sendCmd(12.chr, path, input)
    end

    def store(path, input)
      sendCmd(13.chr, path, input)
    end

    def info()
      return @info
    end

    def close()
      send("exit")
      @socket.close
    end

    # Receives a string from the socket.
    def receive()
      complete = ""
      while (t = read) != 0.chr
        if t == @char_lead_byte then
          t = read
        end
        complete << t
      end
      return complete
    end
    
    # Sends the defined str.
    def send(str)
      @socket.write(str + 0.chr)
    end

    def sendCmd(cmd, arg, input)
      send(cmd + arg + 0.chr + input)
      @info = receive
      if !ok
        raise @info
      end
    end

    # Returns a single byte from the socket.
    def read()
      return @socket.read(1)
    end
    
    def write(i)
      @socket.write(i)
    end
    
    # Returns success check. 
    def ok()
      return read == 0.chr
    end
  end

  class Query
    def initialize(s, q)
      @session = s
      @id = exec(0.chr, q)
      @cache = []
      @pos = 0
    end

    def bind(name, value, type="")
      exec(3.chr, @id + 0.chr + name + 0.chr + value + 0.chr + type)
    end

    def context(value, type="")
      exec(14.chr, @id + 0.chr + value + 0.chr + type)
    end

    def more()
      if @cache.length == 0
        @session.write(4.chr)
        @session.send(@id)
        while @session.read > 0.chr
          @cache << @session.receive
        end
        if !@session.ok
          raise @session.receive
        end
      end
      return @pos < @cache.length
    end

    def next
      if more()
        @pos += 1
        return @cache[@pos - 1]
      end
    end

    def execute()
      return exec(5.chr, @id)
    end
    
    def info()
      return exec(6.chr, @id)
    end
    
    def close()
      return exec(2.chr, @id)
    end
    
    def exec(cmd, arg)
      @session.send(cmd + arg)
      s = @session.receive
      if !@session.ok
        raise @session.receive
      end
      return s
    end
  end
end
