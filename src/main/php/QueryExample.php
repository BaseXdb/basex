<?php
/*
 * This example shows how queries can be executed in an iterative manner.
 * Iterative evaluation will be slower, as more server requests are performed.
 *
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-12, BSD License
 */
include("BaseXClient.php");

try {
  // create session
  $session = new Session("localhost", 1984, "admin", "admin");
  
  try {
    // create query instance
    $input = 'for $i in 1 to 10 return <xml>Text { $i }</xml>';
    $query = $session->query($input);

    // loop through all results
    while($query->more()) {
      print $query->next()."<br/>";
    }

    // close query instance
    $query->close();

  } catch (Exception $e) {
    // print exception
    print $e->getMessage();
  }

  // close session
  $session->close();

} catch (Exception $e) {
  // print exception
  print $e->getMessage();
}
?>
