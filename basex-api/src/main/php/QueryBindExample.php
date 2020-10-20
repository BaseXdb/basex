<?php
/*
 * This example shows how external variables can be bound to XQuery expressions.
 *
 * Documentation: https://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-12, BSD License
 */
include_once 'load.php';

use BaseXClient\BaseXException;
use BaseXClient\Session;

try {
    // create session
    $session = new Session("localhost", 1984, "admin", "admin");

    try {
        // create query instance
        $input = 'declare variable $name external; for $i in 1 to 10 return element { $name } { $i }';
        $query = $session->query($input);

        // bind variable
        $query->bind("name", "number");

        // print results
        print $query->execute()."\n";

        // close query instance
        $query->close();
    } catch (BaseXException $e) {
        // print exception
        print $e->getMessage();
    }

    // close session
    $session->close();
} catch (BaseXException $e) {
    // print exception
    print $e->getMessage();
}
