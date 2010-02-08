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
        self.host = host
        self.port = port
        self.user = user
        self.pw = pw
        
    # Connects to the server.    
    def connect(self):
        self.s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.s.connect((self.host,self.port)) 
        return self.login()
    
    # Login of user at the server.
    def login(self):
        ts = self.getIt()
        pwmd5 = hashlib.md5(self.pw).hexdigest()
        m = hashlib.md5()
        m.update(pwmd5)
        m.update(ts)
        complete = m.hexdigest()
        self.sendCommand(self.user)
        self.sendCommand(complete)
        data = self.s.recv(1)
        return "\0" == data
    
    # sends command to the server        
    def sendCommand(self,com):
        self.s.send(str.encode(com))
        self.s.send("\0")
    
    # Gets the parts of the answer.    
    def getIt(self):
        com = ""
        while True:
            data = self.s.recv(1)
            if(data == "\0"):
                return com
            else:
                com += data
                      
    # Receives data.    
    def receive(self):
        part1 = self.getIt()
        part2 = self.getIt()
        part3 = self.s.recv(1)
        recv = ""
        if part1 != "\0":
            recv += part1 + part2
        else:
            recv = part2
        return recv
    
    # Sends command and receives answer.
    def execute(self, com):
        self.sendCommand(com)
        return self.receive()
    
    # Closes the connection.       
    def close(self):
        self.s.close()