<?php
/*
 * This PHP Module provides two classes for connecting to the
 * BaseX Server.
 *
 * The Session class manages the communication between server and client.
 * This class has to be called by your client code (see Example.php).
 *
 * The Constructor of the Session class expects a hostname, port, username and
 * password for the connection. The socket connection will then be established
 * via the hostname and the port.
 *
 * For the execution of commands, you need to specify an output stream, which
 * is passed on to the execute method.
 *
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 */
class Session {
  /* Class variables.*/
  var $socket, $result, $info, $buffer, $bpos, $bsize;

  /* Constructor. */
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

  /* Executes a command and writes the result to the specified stream. */
  public function execute($com, $out = null) {
    // send command to server
    socket_write($this->socket, "$com\0");

    // receive result
    $this->init();
    $this->result = $this->readString();
    $this->info = $this->readString();

    // send output, if stream was specified
    if($out) {
      // wrap output reference to resource
      if(!is_resource($out)) $out = fopen($out, "w");
      fwrite($out, $this->result);
    }
    return $this->read() == "\0";
  }

  /* Returns the result. */
  public function result() {
    return $this->result;
  }

  /* Returns the info string. */
  public function info() {
    return $this->info;
  }

  /* Closes the socket. */
  public function close() {
    socket_write($this->socket, "exit\0");
    socket_close($this->socket);
  }

  /* Initiates the incoming message. */
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

  /* Returns the next byte. */
  private function read() {
    if($this->bpos == $this->bsize) {
      $this->bsize = socket_recv($this->socket, $this->buffer, 4096, 0);
      $this->bpos = 0;
    }
    return $this->buffer[$this->bpos++];
  }
}
?>
