<?php
include ("BaseX.php");
try {
	$session = new Session("localhost", 1920, "admin", "admin");
	$session->execute("help", "php://stdout");
} catch (Exception $e) {
	echo $e->getMessage();
}
?>
