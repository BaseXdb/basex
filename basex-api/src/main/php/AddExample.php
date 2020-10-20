<?php
/*
 * This example shows how new documents can be added.
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

    // create new database
    $session->execute("create db database");
    print $session->info();

    // add document
    $session->add("world/World.xml", "<x>Hello World!</x>");
    print "<br/>".$session->info();

    // add document
    $session->add("Universe.xml", "<x>Hello Universe!</x>");
    print "<br/>".$session->info();

    // run query on database
    print "<br/>".$session->execute("xquery /");

    // drop database
    $session->execute("drop db database");

    // close session
    $session->close();
} catch (BaseXException $e) {
    // print exception
    print $e->getMessage();
}
