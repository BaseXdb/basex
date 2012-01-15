# This example shows how queries can be executed in an iterative manner.
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
  my $input = "for \$i in 1 to 10 return <xml>Text { \$i }</xml>";
  my $query = $session->query($input);

  # loop through all results
  while ($query->more()) {
    print $query->next()."\n";
  }

  # close query
  $query->close();

  # close session
  $session->close();
};

# print exception
print $@ if $@;