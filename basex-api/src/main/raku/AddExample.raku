# This example shows how new documents can be added.
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

	# create empty database
	my %results;
	%results = $session.command("create db database");
	say %results<info>;
  
	# add document
	%results = $session.add("world/World.xml", "<x>Hello World!</x>");
	say %results<info>;
  
	# add document
	%results = $session.add("Universe.xml", "<x>Hello Universe!</x>");
	say %results<info>; 
  
	# run query on database
	say $session.command("xquery collection('database')")<result>;
  
	# drop database
	$session.command("drop db database");
  
	# close session
	$session.close();

	CATCH {
		# print exception
		say $*ERR;
	}
};

