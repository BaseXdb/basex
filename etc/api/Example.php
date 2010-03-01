<?php
/*
 * This example shows how database commands can be performed
 * via the PHP BaseX API.
 * The outputstream in this example is the standard output.
 * The result of the query will be written to this output.
 * 
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 */
include ("BaseX.php");
try {
	$session = new Session("localhost", 1920, "admin", "admin");
	$session->execute("xquery 1 + 2", "php://stdout");
} catch (Exception $e) {
	echo $e->getMessage();
}
?>
