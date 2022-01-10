# This example shows how new databases can be created.
#
# Documentation: https://docs.basex.org/wiki/Clients
#
# (C) BaseX Team 2005-22, BSD License

use BaseXClient;

try {
	# create session
	my $session = Session.new(
		host => "localhost", 
		port => 1984, 
		username => "admin", 
		password => "admin"
	);

	# create new database
	my %results;
	%results = $session.create(name => "database", input => "<x>Hello World!</x>");
	say %results<info>;
  
	# run query on database
	%results = $session.command(command => "xquery doc('database')");
	say %results<result>;
  
	# drop database
	$session.command(command => "drop db database");
  
	# close session
	$session.close();

	CATCH {
		# print exception
		print $*ERR;
	}
};

