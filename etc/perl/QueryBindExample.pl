# This example shows how queries can be executed in an iterative manner.
# Documentation: http://basex.org/api
#
# (C) Workgroup DBIS, University of Konstanz 2005-11, ISC License

use BaseXClient;
use Time::HiRes;
use warnings;
use strict;

eval {
  # create session
  my $session = Session->new("localhost", 1984, "admin", "admin");

  # create query instance
  my $input = "declare variable \$name external; ".
    "for \$i in 1 to 10 return element { \$name } { \$i }";
  my $query = $session->query($input);
	
  # bind variable
  $query->bind("$name", "number");
  		
  # initialize query
  print $query->init();

  # loop through all results
  while ($query->more()) {
    print $query->next()."\n";
  }

  # close query
  print $query->close();

  # close session
  $session->close();
};

# print exception
print $@ if $@;
