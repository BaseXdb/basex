# This example shows how database commands can be executed.
#
# Documentation: https://docs.basex.org/wiki/Clients
#
# (C) BaseX Team 2005-21, BSD License

use BaseXClient;

try {
	# initialize timer
	my $start = DateTime.now();

	# create session
	my $session = Session.new(
		host => "localhost", 
		port => 1984, 
		username => "admin", 
		password => "admin"
	);

	# perform command and print returned string
	my %results;
	%results = $session.command("xquery 1 to 10");
	say %results<result>;

	# close session
	$session.close();

	# print time needed
	my $time = DateTime.now() - $start;
	print "\n$time ms.\n";

	CATCH {
		# print exception
		print $*ERR;
	}
};

