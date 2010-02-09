<?php

function connect($host, $port) {
	global $socket;
	$socket = socket_create(AF_INET, SOCK_STREAM, 0);
	$result = socket_connect($socket, $host, $port); 
    if (!$socket) {
     	echo "Can't communicate with the server.";
    }
    else {
    return login();
    }
}

function execute($com) {
    sendCommand($com);
    return receive();
}

function login() {
    $ts = getIt();
    $pw = hash("md5", "admin");
    $all = $pw.$ts;
    $end = hash("md5", $all);
    sendCommand("admin");
    sendCommand($end);
    $data = read1byte();
    return $data == "\x00";
}

function sendCommand($com) {
	global $socket;
    socket_write($socket, $com);
    socket_write($socket, "\x00");
}

function read1byte() {
	global $socket;
	return socket_read($socket, 1);
}

function readInput() {
    $com = "";
    while (($data = read1byte()) != "\x00") {
    $com = $com.$data;
    }
    return $com;
}

function receive() {
	$part1 = readInput();
    $part2 = readInput();
    $part3 = read1byte();
    $recv = "";
    if ($part1 != "\x00") {
       $recv = $part1.$part2;
    }
    else {
    $recv = $part2;
    }
    return $recv;
}

function closeSocket() {
	global $socket;
    socket_close($socket);
}
?>