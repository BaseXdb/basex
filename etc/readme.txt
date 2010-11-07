
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

 To speedup execution, an output stream can be specified by some bindings; this
 way, all results will be directred to that output stream.

 The query() method creates a query object. Variables can be bound to that
 object, and the result can either be requested by execute(), or by the
 more() and next() methods. If an error occurs, an exception will be thrown.

 Next, some bindings contain the create() and add() method to create new
 database instances or add documents to existing databases.

 Most bindings are accompanied by some example files, demonstrating how
 database commands can be executed and how the query iterator can be used.

CLASS STRUCTURE ----------------------------------------------------------------

class Session:

  // Creates and returns session with host, port, user name and password
  constructor(String host, int port, String name, String password)

  // Executes a command and returns the result
  String execute(String command)

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

  // Binds an external variable
  void bind(String name, String value)

  // Initializes the iterator
  String init()

  // Executes the query
  String execute()

  // Iterator: checks if a query returns more items
  boolean more()

  // Returns next item
  String next()

  // Returns query information
  String info()

  // Closes the iterator and query
  String close()


TRANSFER PROTOCOL (BaseX 6.3.1 ff.) --------------------------------------------

 {...} = string; \n = single byte
 
 Authentication:
 1. Client connects to server socket
 2. Server sends timestamp: {timestamp} \0
 3. Client sends username and hash:
    {username} \0 {md5(md5(password) + timestamp)} \0
 4. Server sends \0 (success) or \1 (error)

 Client transfer:
 - command:       -> {command} \0
 - create:        -> \8 {name} \0 {input} \0
 - add:           -> \9 {name} \0 {target} \0 {input} \0
 - query: start   -> \0 {query} \0
          bind    -> \3 {id} \0 {variable} \0 {value}\0 {type}\0
          execute -> \5 {id} \0
          info    -> \6 {id} \0
          next    -> \1 {id} \0
          init    -> \4 {id} \0
          end     -> \2 {id} \0

 Server streams:
 - command:       -> {result} \0 {info} \0 \0
 - create:        -> {info} \0 \0
 - add:           -> {info} \0 \0
          error   -> \0 {error} \0 \1
 - query: start   -> {id} \0 \0
          bind    -> \0 \0
          execute -> {result} \0 \0
          init    -> {result} \0 \0
          next    -> {result} \0 \0
          info    -> {result} \0 \0
          close   -> {result} \0 \0
          error   -> \0 \1 {error} \0

============================================= DBIS Group, University of Konstanz
