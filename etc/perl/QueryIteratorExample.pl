# -----------------------------------------------------------------------------
#
# This example shows how results from a query can be received in an iterative
# mode.
# The execution time will be printed along with the result of the command.
#
# -----------------------------------------------------------------------------
# (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
# -----------------------------------------------------------------------------

use BaseXClient;
use Time::HiRes;
use warnings;
use strict;

# initialize timer
my $start = [ Time::HiRes::gettimeofday( ) ];

# command to be performed
my $cmd = "1 to 10";

eval {
  # create session
  my $session = Session->new("localhost", 1984, "admin", "admin");

  # create and run query
  my $query = $session->query($cmd);
  while ($query->more()) {
    print " - " , $query->next();
  }
  # close query object
  $query->close();

  # close session
  $session->close();

  # print time needed
  my $time = Time::HiRes::tv_interval($start) * 1000;
  print "\n$time ms.\n";
};

# print exception
print $@ if $@;