<?php
/* ----------------------------------------------------------------------------
 *
 * This PHP module provides methods to connect to and communicate with the
 * BaseX Server.
 *
 * The Constructor of the class expects a hostname, port, username and password
 * for the connection. The socket connection will then be established via the
 * hostname and the port.
 *
 * For the execution of commands you need to call the execute() method with the
 * database command as argument. The method returns a boolean, indicating if
 * the command was successful. The result can be requested with the result()
 * method, and the info() method returns additional processing information
 * or error output.
 *
 * ----------------------------------------------------------------------------
 *
 * Example:
 *
 * include("BaseXClient.php");
 *
 * try {
 *   // create session
 *   $session = new Session("localhost", 1984, "admin", "admin");
 *
 *   // perform command and show result or error output
 *   if($session->execute("xquery 1 to 10")) {
 *     print $session->result();
 *   } else {
 *     print $session->info();
 *   }
 *
 *  // close session
 *  $session->close();
 *
 * } catch (Exception $e) {
 *   // print exception
 *   print $e->getMessage();
 * }
 *
 * ----------------------------------------------------------------------------
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * ----------------------------------------------------------------------------
 */
class Session {
  /* Class variables.*/
  var $socket, $result, $info, $buffer, $bpos, $bsize;

  /* Constructor, creating a new socket connection. */
  function __construct($h, $p, $user, $pw) {
    // create server connection
    $this->socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
    if(!socket_connect($this->socket, $h, $p)) {
      throw new Exception("Can't communicate with server.");
    }

    // receive timestamp
    $ts = $this->readString();

    // send username and hashed password/timestamp
    $md5 = hash("md5", hash("md5", $pw).$ts);
    socket_write($this->socket, "$user\0$md5\0");

    // receives success flag
    if(socket_read($this->socket, 1) != "\0") {
      throw new Exception("Access denied.");
    }
  }

  /* Executes the specified command. */
  public function execute($com) {
    // send command to server
    socket_write($this->socket, "$com\0");

    // receive result
    $this->init();
    $this->result = $this->readString();
    $this->info = $this->readString();

    return $this->read() == "\0";
  }

  /* Returns the result. */
  public function result() {
    return $this->result;
  }

  /* Returns processing information. */
  public function info() {
    return $this->info;
  }

  /* Closes the connection. */
  public function close() {
    socket_write($this->socket, "exit\0");
    socket_close($this->socket);
  }

  /* Initializes the byte transfer */
  private function init() {
    $this->bpos = 0;
    $this->bsize = 0;
  }

  /* Receives a string from the socket. */
  private function readString() {
    $com = "";
    while(($d = $this->read()) != "\0") {
      $com .= $d;
    }
    return $com;
  }

  /* Returns a single byte from the socket. */
  private function read() {
    if($this->bpos == $this->bsize) {
      $this->bsize = socket_recv($this->socket, $this->buffer, 4096, 0);
      $this->bpos = 0;
    }
    return $this->buffer[$this->bpos++];
  }
  
  public function query($q) {
  	return new Query($this, $q);
  }
  
  public function executeIter($cmd) {
  	// send command to server
    socket_write($this->socket, "\0$com\0");
    return $this->check();
  }
  
  public function send($sign) {
  	socket_write($this->socket, $sign);
  }
  
  public function check() {
  	return socket_read($this->socket, 1) == "\0";
  }
  
  public function res() {
  	$this->init();
  	return $this->readString();
  }
}

class Query {
	/* Class variables.*/
 	var $session, $query, $part;
 	
	function __construct($s, $q) {
		$this->session = $s;
		$this->query = $q;
	}
	
	public function run() {
		return $session->executerIter($query);
	}
	
	public function more() {
		$session->send("\0");
		if ($session->check()) {
			$this->part = $session->res();
			return True;
		} else {
			return False;
		}
	}
	
	public function next() {
		return $part;
	}
	
	public function abort() {
		$session->send("\1");
	}
	
	public function info() {
		return $session->res();
	}	
}
?>
