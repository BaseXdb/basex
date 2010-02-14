"""/**
 * This is the starter class for the client console mode.
 * It sends all commands to the server instance.
 * Add the '-h' option to get a list on all available command-line arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andeas Weiler
 */"""
 
import ClientSession, getopt, sys, getpass

class BaseXClient(object):
    # Initializes the client.
    def __init__(self,host,port):
        self.host = host
        self.port = port
        print 'BaseX 6.01 [Client]'
        print 'Try "help" to get some information.'           
    
    # Creates a session.   
    def session(self):
        user = raw_input('Username: ');
        pw = getpass.getpass('Password: ');
        global session
        session = ClientSession.ClientSession(self.host, self.port, user, pw)
        return session.connect()
    
    # Reads commands from the console.    
    def readCommand(self):
        com = raw_input('> ')
        if com == "":
            self.readCommand()
        return com.strip()
    
    # Runs the console.
    def console(self):
        if self.session() == True:
            session.execute("SET INFO ON")
            session.result()
            while True:
                com = self.readCommand()
                if com == "exit":
                    session.execute("exit")
                    break
                if session.execute(com) == "\0":
                    res = session.result()
                    if res != "":
                        print res
                    inf = session.info()
                    if inf != "":
                        print inf                    
                else:
                    session.result()
                    print session.info()
            try: 
                session.execute("exit")
            except:
                session.close()
            print "See you."
        else:
            print "Access denied."
            session.close()

# Reads arguments -p and -h.
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

# Main method.
if __name__ == '__main__':
    opts()
    bxc = BaseXClient(host,port)
    bxc.console()