
BASEX LANGUAGE BINDINGS ========================================================

 In this directory, you find bindings to communicate with the database server
 in different programming languages. If your projects are based on other
 languages, or if you feel that the existing bindings could be improved:
 Feedback is welcome.

 BaseX Team, 2010


DESCRIPTION --------------------------------------------------------------------

 The Session class provides methods to connect to and communicate with the
 database server.

 A socket connection will be established by the constructor, which expects a
 hostname, port, username and password as arguments.

 For the execution of commands you need to call the execute() method with the
 database command as argument. The method returns the result or throws
 an exception with the received error message.

 Some bindings offer a faster execute() method, which will write the result to
 a specified output stream.

 The query() method creates an iterator for the specified query. The resulting
 items will be returned via the more() and next() methods. If an error occurs,
 an exception will be thrown.

 Last but not least, some bindings offer the create() method as an efficient
 solution to create new database instances.

 Most bindings are accompanied by some example files, demonstrating how
 database commands can be executed and how the query iterator can be used.


CLASS STRUCTURE ----------------------------------------------------------------

class Session:

  // Creates and returns session with host, port, user name and password
  constructor(String host, int port, String name, String password)

  // Executes a command and returns the result
  String execute(String command)

  // Executes a command and writes the result to an output stream
  void execute(String command, OutputStream out)

  // Returns a query object for the specified query
  Query query(String query)

  // Creates a database from an input stream
  void create(String name, InputStream in)

  // Returns process information
  String info()

  // Closes the session
  void close()


class Query:

  // Creates query object with session and query
  contructor(Session s, String query)

  // Checks for next item of the result and returns boolean
  boolean more()

  // Returns next result item
  String next()

  // Closes the query object
  void close()


STREAM PROTOCOL ----------------------------------------------------------------

 {} = item or flag; 0 = marker for end of item
 
 Authentication:
 1. Client connects to server socket
 2. Server sends timestamp
 3. Client sends username and hash: username 0 md5(md5(password) + timestamp) 0
 4. Server sends 0 (successful) or 1 (error)

 Client streams:
 - standard mode:        -> {command} 0
 - iterative mode: start -> {0} {query} 0
                   next  -> {1} {id} 0
                   end   -> {2} {id} 0

 Server streams:
 - standard mode:  success -> {result} 0 {info} 0 {0}
                   error   -> {0} 0 {error} 0 {1}
 - iterative mode: start   -> {id} 0 {0}
                   next    -> {item} 0 {0}
                   error   -> {0} {1} {error}


============================================= DBIS Group, University of Konstanz
