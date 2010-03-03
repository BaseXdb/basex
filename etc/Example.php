<?php
/*
 * This example shows how database commands can be performed
 * via the PHP BaseX API.
 *
 * The outputstream in this example is the standard output.
 * The result of the query will be written to this output.
 *
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 */
include("BaseX.php");

// initialize timer
$start = microtime(true);

// output stream
//$out = fopen("result.tmp", "w");
$out = "php://output";

// command to be performed
//$cmd = "xquery doc('11MB')//item";
$cmd = "xquery 1 to 100";

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
  print "\n".((microtime(true) - $start) * 1000)." ms";

  // close session
  $session->close();

} catch (Exception $e) {
	print $e->getMessage();
}
?>
