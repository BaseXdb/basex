# This example shows how external variables can be bound to XQuery expressions.
#
# Documentation: https://docs.basex.org/wiki/Clients
#
# (C) BaseX Team 2005-21, BSD License

use BaseXClient;

sub	MAIN($username = 'admin', :$ask-password = 0) {
	my $password = $ask-password ?? prompt('Enter password: ') !! 'admin';

	try {
		# create session
		my $session = Session.new(
			host => "localhost4", 
			port => 1984, 
			username => $username, 
			password => $password,
		);

		# create query instance
		my $input = 'declare variable $name external; for $i in 1 to 10 return element { $name } { $i }';
		my $query = $session.query(query => $input);
	
		# bind variable
		$query.bind(name => "name", value => "number");

		# print result
		say $query.execute()<result>;

		# close query
		$query.close();

		# close session
		$session.close();

		CATCH {
			# print exception
			say $*ERR;
		}
	};
}

