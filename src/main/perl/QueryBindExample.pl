# This example shows how external variables can be bound to XQuery expressions.
#
# Documentation: http://docs.basex.org/wiki/Clients
#
# (C) BaseX Team 2005-12, BSD License

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
  $query->bind("name", "number");

  # print result
  print $query->execute()."\n";

  # close query
  $query->close();

  # close session
  $session->close();
};

# print exception
print $@ if $@;
