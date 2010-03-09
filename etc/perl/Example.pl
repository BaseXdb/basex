# -----------------------------------------------------------------------------
#
# This example shows how BaseX commands can be performed via the Perl API.
# The execution time will be printed along with the result of the command.
#
# -----------------------------------------------------------------------------
# (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
# -----------------------------------------------------------------------------

use BaseX;
use Time::HiRes;
use warnings;
use strict;

# initialize timer
my $start = [ Time::HiRes::gettimeofday( ) ];

# command to be performed
my $cmd = "xquery 1 to 10";

eval {
  # create session
  my $session = BaseX->new("localhost", 1984, "admin", "admin");

  # perform command and show result or error output
  if($session->execute($cmd)) {
    print $session->result();
  } else {
    print $session->info();
  }

  # close session
  $session->close();

  # print time needed
  my $time = Time::HiRes::tv_interval($start) * 1000;
  print "\n$time ms.\n";
};

# print exception
print $@ if $@;
