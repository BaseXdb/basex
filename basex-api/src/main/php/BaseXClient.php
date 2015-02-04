<?php
/*
 * PHP client for BaseX.
 * Works with BaseX 7.0 and later
 *
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-15, BSD License
 */
class Session {
  // class variables.
  var $socket, $info, $buffer, $bpos, $bsize;

  function __construct($h, $p, $user, $pw) {
    // create server connection
    $this->socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
    if(!socket_connect($this->socket, $h, $p)) {
      throw new Exception("Can't communicate with server.");
    }

    // receive timestamp
    $ts = $this->readString();
	  // Hash container
    if (false !== strpos($ts, ':')) {
      // digest-auth
      $challenge = explode(':', $ts, 2);
      $md5 = hash("md5", hash("md5", $user . ':' . $challenge[0] . ':' . $pw) . $challenge[1]);
    } else {
      // Legacy: cram-md5
      $md5 = hash("md5", hash("md5", $pw) . $ts);
    }

    // send username and hashed password/timestamp
    socket_write($this->socket, $user . chr(0) . $md5 . chr(0));

    // receives success flag
    if(socket_read($this->socket, 1) != chr(0)) {
      throw new Exception("Access denied.");
    }
  }

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

  public function query($q) {
    return new Query($this, $q);
  }

  public function create($name, $input) {
    $this->sendCmd(8, $name, $input);
  }

  public function add($path, $input) {
    $this->sendCmd(9, $path, $input);
  }

  public function replace($path, $input) {
    $this->sendCmd(12, $path, $input);
  }

  public function store($path, $input) {
    $this->sendCmd(13, $path, $input);
  }

  public function info() {
    return $this->info;
  }

  public function close() {
    socket_write($this->socket, "exit".chr(0));
    socket_close($this->socket);
  }

  private function init() {
    $this->bpos = 0;
    $this->bsize = 0;
  }

  public function readString() {
    $com = "";
    while(($d = $this->read()) != chr(0)) {
      $com .= $d;
    }
    return $com;
  }

  private function read() {
    if($this->bpos == $this->bsize) {
      $this->bsize = socket_recv($this->socket, $this->buffer, 4096, 0);
      $this->bpos = 0;
    }
    return $this->buffer[$this->bpos++];
  }

  private function sendCmd($code, $arg, $input) {
    socket_write($this->socket, chr($code).$arg.chr(0).$input.chr(0));
    $this->info = $this->receive();
    if($this->ok() != True) {
      throw new Exception($this->info);
    }
  }

  public function send($str) {
    socket_write($this->socket, $str.chr(0));
  }

  public function ok() {
    return $this->read() == chr(0);
  }

  public function receive() {
    $this->init();
    return $this->readString();
  }
}

class Query {
  var $session, $id, $open, $cache;

  public function __construct($s, $q) {
    $this->session = $s;
    $this->id = $this->exec(chr(0), $q);
  }

  public function bind($name, $value, $type = "") {
    $this->exec(chr(3), $this->id.chr(0).$name.chr(0).$value.chr(0).$type);
  }

  public function context($value, $type = "") {
    $this->exec(chr(14), $this->id.chr(0).$value.chr(0).$type);
  }

  public function execute() {
    return $this->exec(chr(5), $this->id);
  }

  public function more() {
    if($this->cache == NULL) {
      $this->pos = 0;
      $this->session->send(chr(4).$this->id.chr(0));
      while(!$this->session->ok()) {
        $this->cache[] = $this->session->readString();
      }
      if(!$this->session->ok()) {
        throw new Exception($this->session->readString());
      }
    }
    if($this->pos < count($this->cache)) return true;
    $this->cache = NULL;
    return false;
  }

  public function next() {
    if($this->more()) {
      return $this->cache[$this->pos++];
    }
  }

  public function info() {
    return $this->exec(chr(6), $this->id);
  }

  public function options() {
    return $this->exec(chr(7), $this->id);
  }

  public function close() {
    $this->exec(chr(2), $this->id);
  }

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
