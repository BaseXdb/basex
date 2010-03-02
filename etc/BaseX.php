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

  // Constructor.
  function __construct($h, $p, $user, $pw) {
    global $socket;
    $socket = socket_create(AF_INET, SOCK_STREAM, 0);
    $result = socket_connect($socket, $h, $p);

    if (!$result) {
      throw new Exception("Can't communicate with the server.");
    } else {
      // receive timestamp
      $ts = $this->readString();
      $pw = hash("md5", $pw);
      $md5 = hash("md5", $pw.$ts);

      // send username and hashed password/timestamp
      socket_write($socket, "$user\x00$md5\x00");

      // receives success flag
      if(socket_read($socket, 1) != "\x00") {
        throw new Exception("Access denied.");
      }
    }
  }

  // Executes a command.
  public function execute($com, $out) {
    global $socket;
    global $info;

    // send command to server
    socket_write($socket, "$com\x00");

    // wrap output reference to resource
    if(!is_resource($out)) $out = fopen($out, "w");

    // receive result
    fwrite($out, $this->readString());
    $info = $this->readString();
    return socket_read($socket, 1) == "\x00";
  }

  // Returns the info string.
  public function info() {
    global $info;
    return $info;
  }

  // Closes the socket.
  public function close() {
    global $socket;
    socket_close($socket);
  }

  // Initiates the incoming message.
  private function init() {
    global $message;
    global $pos;
    $message = "";
    $pos = 0;
  }

  // Receives a string from the socket.
  private function readString() {
    global $socket;
      $com = "";
      while (($data = socket_read($socket, 1)) != "\x00") {
        $com .= $data;
      }
      return $com;
  }
}
?>
