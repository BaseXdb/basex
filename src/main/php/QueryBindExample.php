<?php
/*
 * This example shows how queries can be executed in an iterative manner.
 * Documentation: http://basex.org/api
 *
 * (C) BaseX Team 2005-11, BSD License
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
    $query->bind("\$name", "number");

    // initialize query
    print $query->init();

    // loop through all results
    while($query->more()) {
      print htmlspecialchars($query->next())."<br/>";
    }

    // close query instance
    print $query->close();

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
