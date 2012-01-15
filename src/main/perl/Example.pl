# This example shows how database commands can be executed.
#
# Documentation: http://docs.basex.org/wiki/Clients
#
# (C) BaseX Team 2005-12, BSD License

use BaseXClient;
use Time::HiRes;
use warnings;
use strict;

eval {
  # initialize timer
  my $start = [ Time::HiRes::gettimeofday( ) ];

  # create session
  my $session = Session->new("localhost", 1984, "admin", "admin");

  # perform command and print returned string
  print $session->execute("xquery 1 to 10");

  # close session
  $session->close();

  # print time needed
  my $time = Time::HiRes::tv_interval($start) * 1000;
  print "\n$time ms.\n";
};

# print exception
print $@ if $@;
