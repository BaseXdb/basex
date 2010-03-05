# This example shows how database commands can be performed
# via the Perl BaseX API.
# After all, the execution time of the query will be printed.
#
# (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
use BaseX;
use Time::HiRes;
use strict;
use warnings;

my $start = [ Time::HiRes::gettimeofday( ) ];
my $cmd = "xquery 1 to 10";
eval {
my $session = new BaseX("localhost", 1984, "admin", "admin");
if ($session->execute($cmd)) {
	print $session->result();
} else {
	print $session->info();
}

my $diff = Time::HiRes::tv_interval($start) * 1000;
print "\n\n".$diff." ms";

$session->close();
};
if ($@) {
	print $@;
}