<?php
class Session {
	
	// Constructor.
	function __construct($h, $p, $user, $pw) {
		global $socket;
		$socket = socket_create(AF_INET, SOCK_STREAM, 0);
		socket_connect($socket, $h, $p);
		
		// receive timestamp
		$ts = $this->readString();
		$pw = hash("md5", $pw);
    	$pwts = $pw.$ts;
    	$complete = hash("md5", $pwts);
    	
    	// send username and hashed password/timestamp
    	socket_write($socket, $user);
    	socket_write($socket, "\x00");
    	socket_write($socket, $complete);
    	socket_write($socket, "\x00");
    	
    	// receives success flag
    	if (socket_read($socket, 1) != "\x00") {
    		throw new Exception("Access denied.");
    	}		
	}
	
	// Executes a command.
	public function execute($com, $out) {
		global $socket;
		global $info;
		
		socket_write($socket, $com);
		socket_write($socket, "\x00");
		fwrite($out, $this->readString());
		$info = $this->readString();
		
		return $this->read();
	}
	
	// Returns the info string.
	public function info() {
		global $info;
		return $info;
	}
	
	// Receives a string from the socket.
	private function readString() {
	}
	
	// Returns the next byte
	private function read() {
		global $socket;
	}
	
	public function closeSocket() {
		global $socket;
		socket_close($socket);
	} 	
}

class Client {
	
	// Constructor.
	function __construct($h, $p) {
		global $host;
		global $port;
		$host = $h;
		$port = $p;
	}
	
	// Creates a session.
	function session() {
		global $host;
		global $port;
		$username = "admin";
		$password = "admin";
		try {
			new Session($host, $port, $username, $password);
		} catch (Exception $e) {
			echo $e->getMessage();
		}
	}
}

$host = "localhost";
$port = 1984;
$client = new Client($host, $port);
$client -> session();
?>


