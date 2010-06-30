 
 BASEX LANGUAGE BINDINGS ==============================================

 In this directory you find bindings to communicate with the BaseX
 server in different programming languages. If your projects are based
 on other languages, or if you feel that the existing bindings could
 be further improved... Please don't hesitate to give us feedback.
 
 BaseX team 2010

 
DESCRIPTION  ===========================================================

class Session:

  // Creates and returns session with host, port, username and password
  constructor(String host, int port, String username, String password)

  // Executes the specified command and returns the result
  // @return String
  execute(String command)

  // Returns a query object with the specified query
  // @return Query
  query(String query)

  // Returns process information
  // @return String
  info()

  // Closes the session
  close()

class Query:

  // Creates query object with session and query
  contructor(Session s, String query)

  // Checks for next part of the result and returns boolean
  // @return boolean
  more()

  // Returns next part of the result
  // @return String
  next()

  // Closes the query object
  close()
 
 
 STREAM PROTOCOL =======================================================
 
 {} = item or flag; 0 = marker for end of item
 
 Client streams:
 standard mode:        -> {command}0
 iterative mode: start -> {0}{query}0
 				 next  -> {1}{id}0
 				 end   -> {2}{id}0
 				
 Server streams:
 standard mode:  success       -> {result}0{pi}0{0}
 				 error         -> {0}0{error}0{1}
 iterative mode: start success -> {id}0{0}
 				 next success  -> {resultitem}0{0}
 				 error 		   -> {0}{1}{error}
 				 
 =================================== DBIS Group, University of Konstanz