# This example shows how queries can be executed in an iterative manner.
#
# Documentation: https://docs.basex.org/wiki/Clients
#
# (C) BaseX Team 2005-21, BSD License

use BaseXClient;

try {
	# create session
	my $session = Session.new(
		host => "localhost", 
		port => 1984, 
		username => "admin", 
		password => "admin"
	);

	# create query instance
	my $input = 'for $i in 1 to 10 return <xml>Text { $i }</xml>';
	my $query = $session.query(command => $input);

	# loop through all results
	while ($query.more()) {
		say $query.next();
	}

	# close query
	$query.close();

	# close session
	$session.close();

	CATCH {
		# print exception
		print $*ERR;
	}
};

