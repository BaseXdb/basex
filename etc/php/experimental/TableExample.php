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
include("BaseXClient.php");

$createDB = "create db <countries><c>Brasil</c><c>Italy</c><c>Germany</c><c>Uruguay</c><c>Argentina</c><c>England</c><c>France</c></countries> wm";

// command to be performed
$cmd = "doc('factbook')//country";

try {
  // create session
  $session = new Session("localhost", 1984, "admin", "admin");
  
  
  $session->execute($createDB);
  echo "<table border='0' cellspacing='2' cellpadding='4' width='20%'>
	<tbody><tr style='text-align:center;'><tbody><tr style='text-align:center;'>";
  echo "<td style='text-align:center;background-color:#D7D7D7;border:#ffffff 1px solid;font-size:12pt;'></td>";
  echo "<td style='text-align:center;background-color:#D7D7D7;border:#ffffff 1px solid;font-size:12pt;'>Country</td>";
  $query = $session->query($cmd);
  $count = 0;
  if($query->run()) {
  	while($query->more()) {
  		$next = $query->next();
  		$count += 1;
  		if($count%2) {
  		echo "<tr style='text-align:center;'>
  		<td style='text-align:center;'>$count</td><td style='text-align:center;'>$next</td></tr>";
  		} else {
  		echo "<tr style='text-align:center; background-color:#eeeeee;'>
  		<td style='text-align:center;'>$count</td><td style='text-align:center;'>$next</td></tr>";
  		}
  	}
  } else {
  	print $query->info();
  }
  echo "</table>";

  // close session
  $session->close();

} catch (Exception $e) {
  // print exception
  print $e->getMessage();
}
?>
