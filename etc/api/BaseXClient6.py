""" /**
 * Python BaseXClient for BaseX 6.0.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */ """

import hashlib, socket, sys, getopt

class BaseXClient6(object):
    def __init__(self,host,port):
        self.host = host
        self.port = port
        print 'BaseX 6.01 [Client]'
        print 'Try "help" to get some information.'
    
    # connects to the server    
    def connect(self):
        self.s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.s.connect((self.host,self.port))
        return self.login()              
    
    # login of user at the server
    def login(self):
        ts = self.getIt()
        user = raw_input('Username: ');
        pw = raw_input('Password: ');
        pwmd5 = hashlib.md5(pw).hexdigest()
        m = hashlib.md5()
        m.update(pwmd5)
        m.update(ts)
        complete = m.hexdigest()
        self.sendCommand(user)
        self.sendCommand(complete)
        data = self.s.recv(1)
        return "\0" == data
    
    # sends command to the server        
    def sendCommand(self,com):
        self.s.send(str.encode(com))
        self.s.send("\0")
        
    def getIt(self):
        com = ""
        while True:
            data = self.s.recv(1)
            if(data == "\0"):
                return com
            else:
                com += data
                      
    # receives data    
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
    
    # reads commands from the console    
    def readCommand(self):
        self.com = raw_input('> ')
        return self.com
    
    # runs the console
    def console(self):
        if self.connect() == True:
            self.sendCommand("SET INFO ON")
            self.s.recv(1024)
            while self.readCommand() != "exit":
                self.sendCommand(self.com)
                data = self.receive()
                print data
            try: 
                self.sendCommand("exit")
            except:
                self.close()
            print "See you."
        else:
            print "Access denied."
            self.close()
    
    # closes the connection        
    def close(self):
        self.s.close()

# reads arguments -p and -h
def opts():
        try:
            opts, args = getopt.getopt(sys.argv[1:], "-p:-h", ["port", "host"])
        except getopt.GetoptError, err:
            print str(err)
            sys.exit()
        global host
        global port
        host = "localhost"
        port = 1984
        
        for o, a in opts:
            if o == "-p":
                port = int(a)
            if o == "-h":
                host = a
                    
if __name__ == '__main__':
    opts()
    bxc = BaseXClient6(host,port)
    try: 
        bxc.console()
    except:
        print "Can't communicate with the server."
        sys.exit()