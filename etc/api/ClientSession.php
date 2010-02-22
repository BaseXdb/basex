<?php

// Initializes the ClientSession.
function connect($host, $port, $user, $pw) {
	global $socket;
	$socket = socket_create(AF_INET, SOCK_STREAM, 0);
	$result = socket_connect($socket, $host, $port); 
    if (!$result) {
     	throw new Exception("Can't communicate with the server.");
    }
    else {
    // receive timestamp
   	$ts = readString();
    $pw = hash("md5", $pw);
    $all = $pw.$ts;
    $end = hash("md5", $all);
    
    // send username and hashed password/timestamp
    socket_write($socket, $user);
    socket_write($socket, "\x00");
    socket_write($socket, $all);
    socket_write($socket, "\x00");
    
    // receives success flag
    if (socket_read($socket, 1) != "\x00") {
    	throw new Exception("Access denied.");
    }	
  }
}

// Executes a command.
function execute($com, $stream) {
	global $socket;
	global $info;
	socket_write($socket, $com);
    socket_write($socket, "\x00");
    fopen($stream, 'w');
    fwrite($stream, readString());
    $info = readString();
    return socket_read($socket, 1);
}

// Returns the info string.
function info() {
	global $info;
	return $info;
}

// Closes the socket.
function closeSocket() {
	global $socket;
    socket_close($socket);
}

// Reads string from the input.
function readString() {
	global $socket;
    $com = "";
    while (($data = socket_read($socket, 1)) != "\x00") {
    $com = $com.$data;
    }
    return $com;
}
?>