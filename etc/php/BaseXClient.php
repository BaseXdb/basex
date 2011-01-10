<?php
/*
 * Language Binding for BaseX.
 * Works with BaseX 6.3.1 and later
 * Documentation: http://basex.org/api
 * 
 * (C) BaseX Team 2005-11, ISC License
 */
class Session {
  /* Class variables.*/
  var $socket, $info, $buffer, $bpos, $bsize;

  /* see readme.txt */
  function __construct($h, $p, $user, $pw) {
    // create server connection
    $this->socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
    if(!socket_connect($this->socket, $h, $p)) {
      throw new Exception("Can't communicate with server.");
    }

    // receive timestamp
    $ts = $this->receive();

    // send username and hashed password/timestamp
    $md5 = hash("md5", hash("md5", $pw).$ts);
    socket_write($this->socket, "$user\0$md5\0");

    // receives success flag
    if(socket_read($this->socket, 1) != "\0") {
      throw new Exception("Access denied.");
    }
  }

  /* see readme.txt */
  public function execute($com) {
    // send command to server
    socket_write($this->socket, "$com\0");

    // receive result
    $result = $this->receive();
    $this->info = $this->receive();
    if($this->ok() != True) {
      throw new Exception($this->info);
    }
    return $result;
  }

  /* see readme.txt */
  public function info() {
    return $this->info;
  }

  /* see readme.txt */
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
  public function receive() {
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
  
  /* Returns the query object.*/
  public function query($q) {
    return new Query($this, $q);
  }
  
  /* Sends the str. */
  public function send($str) {
    socket_write($this->socket, "$str\0");
  }
  
  /* Returns success check. */
  public function ok() {
    return $this->read() == "\0";
  }
  
  /* Returns the result. */
  public function receive() {
    $this->init();
    return $this->receive();
  }
}

class Query {
  /* Class variables.*/
  var $session, $id, $open, $next;
 
  /* see readme.txt */
  function __construct($s, $q) {
    $this->session = $s;
    $this->id = $this->exec("\0", $q);
  }
  
  /* see readme.txt */
  function init() {
    return $this->exec("\4", $this->id);
  }
  
  /* see readme.txt */
  function bind($name, $value) {
    $this->exec("\3", "$this->id\0$name\0$value\0");
  }

  /* see readme.txt */
  public function more() {
    $this->next = $this->exec("\1", $this->id);
    return strlen($this->next) > 0; 
  }

  /* see readme.txt */
  public function next() {
    return $this->next;
  }
  
  /* see readme.txt */
  public function execute() {
    return $this->exec("\5", $this->id);
  }
  
  /* see readme.txt */
  public function info() {
    return $this->exec("\6", $this->id);
  }
  
  /* see readme.txt */
  public function close() {
  	return $this->exec("\2", $this->id);   
  }
  
  /* see readme.txt */
  public function exec($cmd, $arg) {
  	$this->session->send("$cmd$arg");
  	$s = $this->session->receive();
  	if($this->session->ok() != True) {
  	  throw new Exception($this->session->receive());
  	}
  	return $s;
  }
}
?>
