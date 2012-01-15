<?php
/*
 * This example shows how database commands can be executed 
 * and the result of a query can be processed in a dom document.
 *
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-12, BSD License
 */
include("BaseXClient.php");

try {
  // initialize timer
  $start = microtime(true);

  // create session
  $session = new Session("localhost", 1984, "admin", "admin");
  
  
  // dom document
  $dom = new DOMDocument();
  
  $session->execute("create db database <root><user>
  <username>Username1</username><password>Password1</password>
  </user><user><username>Username2</username><password>Password2</password></user></root>");
  
  // perform command and print returned string
  $dom->loadXML($session->execute("xquery ."));
  // print nodes info
  getNodesInfo($dom->firstChild);

  // close session
  $session->close();

  // print time needed
  $time = (microtime(true) - $start) * 1000;
  print "<br/>$time ms\n";

} catch (Exception $e) {
  // print exception
  print $e->getMessage();
}


// print info of node and subnodes
function getNodesInfo($node)
{
  if ($node->hasChildNodes())
  {
  $subNodes = $node->childNodes;
  foreach ($subNodes as $subNode)
    {
	if (($subNode->nodeType != 3)&&($subNode->nodeType != 8)) {
      print "Node name: ".trim($subNode->nodeName)."<br />";
	  print "Node value: ".trim($subNode->nodeValue)."<br />";
	}
	getNodesInfo($subNode);
	}
  }
}
?>
