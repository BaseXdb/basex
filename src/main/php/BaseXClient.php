<?php
/*
 * PHP client for BaseX.
 * Works with BaseX 7.0 and later
 *
 * Documentation: http://docs.basex.org/wiki/Clients
 * 
 * (C) BaseX Team 2005-11, BSD License
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
    $ts = $this->readString();

    // send username and hashed password/timestamp
    $md5 = hash("md5", hash("md5", $pw).$ts);
    socket_write($this->socket, $user.chr(0).$md5.chr(0));

    // receives success flag
    if(socket_read($this->socket, 1) != chr(0)) {
      throw new Exception("Access denied.");
    }
  }

  /* see readme.txt */
  public function execute($com) {
    // send command to server
    socket_write($this->socket, $com.chr(0));

    // receive result
    $result = $this->receive();
    $this->info = $this->readString();
    if($this->ok() != True) {
      throw new Exception($this->info);
    }
    return $result;
  }
  
  /* Returns the query object.*/
  public function query($q) {
    return new Query($this, $q);
  }
  
  /* see readme.txt */
  public function create($name, $input) {
    socket_write($this->socket, chr(8).$name.chr(0).$input.chr(0));
    $this->info = $this->receive();
    if($this->ok() != True) {
      throw new Exception($this->info);
    }
  }
  
  /* see readme.txt */
  public function add($name, $target, $input) {
    socket_write($this->socket, chr(9).$name.chr(0).$target.chr(0).$input.chr(0));
    $this->info = $this->receive();
    if($this->ok() != True) {
      throw new Exception($this->info);
    }
  }

  /* see readme.txt */
  public function replace($path, $input) {
    socket_write($this->socket, chr(12).$path.chr(0).$input.chr(0));
    $this->info = $this->receive();
    if($this->ok() != True) {
      throw new Exception($this->info);
    }
  }

  /* see readme.txt */
  public function store($path, $input) {
    socket_write($this->socket, chr(13).$path.chr(0).$input.chr(0));
    $this->info = $this->receive();
    if($this->ok() != True) {
      throw new Exception($this->info);
    }
  }
  
  /* see readme.txt */
  public function info() {
    return $this->info;
  }

  /* see readme.txt */
  public function close() {
    socket_write($this->socket, "exit".chr(0));
    socket_close($this->socket);
  }

  /* Initializes the byte transfer */
  private function init() {
    $this->bpos = 0;
    $this->bsize = 0;
  }

  /* Receives a string from the socket. */
  public function readString() {
    $com = "";
    while(($d = $this->read()) != chr(0)) {
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
  
  /* Sends the str. */
  public function send($str) {
    socket_write($this->socket, $str.chr(0));
  }
  
  /* Returns success check. */
  public function ok() {
    return $this->read() == chr(0);
  }
  
  /* Returns the result. */
  public function receive() {
    $this->init();
    return $this->readString();
  }
}

class Query {
  /* Class variables.*/
  var $session, $id, $open;
 
  /* see readme.txt */
  function __construct($s, $q) {
    $this->session = $s;
    $this->id = $this->exec(chr(0), $q);
  }
  
  /* see readme.txt */
  function bind($name, $value) {
    $this->exec(chr(3), $this->id.chr(0).$name.chr(0).$value.chr(0));
  }

  /* see readme.txt */
  public function execute() {
    return $this->exec(chr(5), $this->id);
  }
  
  /* see readme.txt */
  public function info() {
    return $this->exec(chr(6), $this->id);
  }
  
  /* see readme.txt */
  public function options() {
    return $this->exec(chr(7), $this->id);
  }
  
  /* see readme.txt */
  public function close() {
  	$this->exec(chr(2), $this->id);   
  }
  
  /* see readme.txt */
  public function exec($cmd, $arg) {
  	$this->session->send($cmd.$arg);
  	$s = $this->session->receive();
  	if($this->session->ok() != True) {
  	  throw new Exception($this->session->readString());
  	}
  	return $s;
  }
}
?>
