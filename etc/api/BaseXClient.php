<?php
include("ClientSession.php");
try {
	connect("localhost", 1920, "admin", "admin");	
} catch (Exception $e) {
	echo $e->getMessage();
}
execute("help", "php://stdout");
closeSocket();
?>