<?php
/*
 * This example shows how database commands can be performed
 * via the PHP BaseX API.
 * After all, the execution time of the query will be printed.
 *
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 */
include("BaseX.php");

// initialize timer
$start = microtime(true);

// command to be performed
//$cmd = "xquery doc('11MB')//item";
$cmd = "xquery 1 to 10";

try {
  // create session
	$session = new Session("localhost", 1984, "admin", "admin");

  // perform command; show info if something went wrong
  if(!$session->execute($cmd)) {
    print $session->info();
  } else {
    print $session->result();
	}

  // print time needed
  print "\n".((microtime(true) - $start) * 1000)." ms\n";

  // close session
  $session->close();

} catch (Exception $e) {
	print $e->getMessage();
}
?>
