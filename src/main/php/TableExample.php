<?php
/*
 * This example shows how results from a query can be received in an iterative
 * mode and illustrated in a table.
 *
 * (C) BaseX Team 2005-11, BSD License
 */
include("BaseXClient.php");

// commands to be performed
$cmd = 'for $node in doc("factbook")//country order by xs:int($node/@population) return data($node/@name)';
$cmd2 = 'for $node in doc("factbook")//country order by xs:int($node/@population) return data($node/@population)';

try {
  // create session
  $session = new Session("localhost", 1984, "admin", "admin");
  
  echo "<table border='0' cellspacing='2' cellpadding='4' width='20%'><tbody><tr style='text-align:center;'>";
  echo "<td style='text-align:center;background-color:#D7D7D7;border:#ffffff 1px solid;font-size:12pt;'></td>";
  echo "<td style='text-align:center;background-color:#D7D7D7;border:#ffffff 1px solid;font-size:12pt;'>Country</td>";
  echo "<td style='text-align:center;background-color:#D7D7D7;border:#ffffff 1px solid;font-size:12pt;'>Population</td>";
  try {
    $query = $session->query($cmd);
    $query2 = $session->query($cmd2);
    $count = 0;
    while($query->more()) {
      $next = $query->next();
      $query2->more();
      $next2 = $query2->next();
      $count += 1;
      if($count%2) {
        echo "<tr style='text-align:center;'>
      <td style='text-align:center;'>$count</td><td style='text-align:center;'>$next</td>
        <td style='text-align:center;'>$next2</td></tr>";
      } else {
      echo "<tr style='text-align:center; background-color:#eeeeee;'>
      <td style='text-align:center;'>$count</td><td style='text-align:center;'>$next</td>
        <td style='text-align:center;'>$next2</td></tr>";
      }
      }
    $query->close();
    $query2->close();
  } catch (Exception $e) {
    // print exception
    print $e->getMessage();
  }  
  echo "</tbody></table>";
  $query->close();
  // close session
  $session->close();

} catch (Exception $e) {
  // print exception
  print $e->getMessage();
}
?>
