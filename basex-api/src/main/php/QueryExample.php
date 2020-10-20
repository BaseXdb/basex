<?php
/*
 * This example shows how queries can be executed in an iterative manner.
 * Iterative evaluation will be slower, as more server requests are performed.
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
        $input = 'for $i in 1 to 10 return <xml>Text { $i }</xml>';
        $query = $session->query($input);

        // loop through all results
        foreach ($query as $resultItem) {
            print $resultItem."\n";
        }

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
