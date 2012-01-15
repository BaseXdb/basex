<?php
/*
 * This example shows how new databases can be created.
 *
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-12, BSD License
 */
include("BaseXClient.php");

try {

  // create session
  $session = new Session("localhost", 1984, "admin", "admin");
  
  // create new database
  $session->create("database", "<x>Hello World!</x>");
  print $session->info();
  
  // run query on database
  print "<br/>".$session->execute("xquery /");
  
  // drop database
  $session->execute("drop db database");

  // close session
  $session->close();

} catch (Exception $e) {
  // print exception
  print $e->getMessage();
}
?>
