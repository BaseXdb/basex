# This example shows how new databases can be created.
# Documentation: http://basex.org/api
#
# (C) BaseX Team 2005-11, BSD License

use BaseXClient;
use warnings;
use strict;

eval {

  # create session
  my $session = Session->new("localhost", 1984, "admin", "admin");

  # create new database
  $session->create("database", "<x>Hello World!</x>");
  print "\n".$session->info()."\n";
  
  # run query on database
  print "\n".$session->execute("xquery doc('database')")."\n";
  
  # drop database
  $session->execute("drop db database");
  
  # close session
  $session->close();
};

# print exception
print $@ if $@;
