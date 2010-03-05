<?php
/* ----------------------------------------------------------------------------
 *
 * This example shows how BaseX commands can be performed via the PHP API.
 * The execution time will be printed along with the result of the command.
 *
 * ----------------------------------------------------------------------------
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * ----------------------------------------------------------------------------
 */
include("BaseX.php");

// initialize timer
$start = microtime(true);

// command to be performed
$cmd = "xquery 1 to 10";

try {
  // create session
  $session = new BaseX("localhost", 1984, "admin", "admin");

  // perform command and show result or error output
  if($session->execute($cmd)) {
    print $session->result();
  } else {
    print $session->info();
  }

  // close session
  $session->close();

  // print time needed
  $time = (microtime(true) - $start) * 1000;
  print "\n$time ms\n";

} catch (Exception $e) {
  // print exception
  print $e->getMessage();
}
?>
