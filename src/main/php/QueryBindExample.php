<?php
/*
 * This example shows how external variables can be bound to XQuery expressions.
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
    $input = "declare variable \$name external; for \$i in 1 to 10 return element { \$name } { \$i }";
    $query = $session->query($input);

    // bind variable
    $query->bind("name", "number");

    // print results
    print $query->execute()."<br/>";

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
