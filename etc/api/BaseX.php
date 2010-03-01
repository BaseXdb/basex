<?php
class Session {
	
	// Constructor.
	function __construct($h, $p, $user, $pw) {
		global $socket;
		$socket = socket_create(AF_INET, SOCK_STREAM, 0);
		$result = socket_connect($socket, $h, $p); 
    	if (!$result) {
     		throw new Exception("Can't communicate with the server.");
    	}
    	else {
    		// receive timestamp
   			$ts = $this->readString();
    		$pw = hash("md5", $pw);
    		$all = $pw.$ts;
    		$end = hash("md5", $all);
    
    		// send username and hashed password/timestamp
    		socket_write($socket, $user);
    		socket_write($socket, "\x00");
    		socket_write($socket, $end);
    		socket_write($socket, "\x00");
    
    	// receives success flag
    	if (socket_read($socket, 1) != "\x00") {
    		throw new Exception("Access denied.");
    	}	
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
		return socket_read($socket, 1);
	}
	
	// Returns the info string.
	public function info() {
		global $info;
		return $info;
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
    		$com = $com.$data;
    	}
    	return $com;
	}
	
	// Closes the socket.
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
			$session = new Session($host, $port, $username, $password);
		} catch (Exception $e) {
			echo $e->getMessage();
		}
	}
}

$host = "localhost";
$port = 1984;
$client = new Client($host, $port);
$client->session();
?>
