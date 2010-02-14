"""/**
 * This wrapper sends commands to the server instance over a socket
 * connection. It extends the {@link Session} class:
 *
 * <ul>
 * <li> A socket instance is created by the constructor.</li>
 * <li> The {@link #execute} method sends database commands to the server.
 * All strings are encoded as UTF8 and suffixed by a zero byte.</li>
 * <li> If the command has been successfully processed,
 * the result string is read.</li>
 * <li> Next, the processing info string is read.</li>
 * <li> A last byte is next sent to indicate if command execution
 * was successful (0) or not (1).</li>
 * <li> {@link #close} closes the session by sending the {@link Cmd#EXIT}
 * command to the server.</li>
 * </ul>
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */"""
 
import hashlib, socket
 
class ClientSession(object):
    
    # Initializes the ClientSession.
    def __init__(self,host,port,user,pw):
        global s
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            s.connect((host, port))
        except:
            print "Can't communicate with the server."
        
        # receive timestamp
        ts = self.readString()
        pwmd5 = hashlib.md5(pw).hexdigest()
        m = hashlib.md5()
        m.update(pwmd5)
        m.update(ts)
        complete = m.hexdigest()
        
        # send user name and hashed password/timestamp
        s.send(str.encode(user))
        s.send("\0")
        s.send(str.encode(complete))
        s.send("\0")
        
        # receives success flag
        if "\0" != s.recv(1):
            raise NameError()
    
    # Executes a command.
    def execute(self,com,out):
        s.send(str.encode(com))
        s.send("\0")
        out.write(self.readString())
        self.info = self.readString()
        return s.recv(1)
               
    # Returns the info string.
    def info(self):
        return self.info
    
    # Closes the socket.
    def close(self):
        s.send(str.encode("exit"))
        s.close()
     
    # Reads strings from the input.    
    def readString(self):
        com = ""
        while True:
            data = s.recv(1)
            if(data == "\0"):
                return com
            else:
                com += data