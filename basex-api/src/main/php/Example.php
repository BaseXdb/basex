<?php
/*
 * This example shows how database commands can be executed.
 *
 * Documentation: https://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-12, BSD License
 */
include_once 'load.php';

use BaseXClient\BaseXException;
use BaseXClient\Session;

try {
    // initialize timer
    $start = microtime(true);

    // create session
    $session = new Session("localhost", 1984, "admin", "admin");

    // perform command and print returned string
    print $session->execute("xquery 1 to 10");

    // close session
    $session->close();

    // print time needed
    $time = (microtime(true) - $start) * 1000;
    print "\n$time ms\n";
} catch (BaseXException $e) {
    // print exception
    print $e->getMessage();
}
