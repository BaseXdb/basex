<?php
/*
 * This example shows how queries can be executed in an iterative manner.
 * Documentation: http://basex.org/api
 *
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 */
include("BaseXClient.php");

try {
  // create session
  $session = new Session("localhost", 1984, "admin", "admin");
  
  try {
    // create query instance
    $input = 'declare variable $n external; for $i in 1 to 10 return <xml> { $n }: { $i } </xml>';
    $query = $session->query($input);
	
	// bind variable
	$query->bind("n", "Number");
	
    // initialize query
    print $query->init();

    // loop through all results
    while($query->more()) {
      print $query->next()."\n";
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
