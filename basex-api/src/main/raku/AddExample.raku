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
	%results = $session.command(command => "create db database");
	say %results<info>;
  
	# add document
	%results = $session.add(path => "world/World.xml", input => "<x>Hello World!</x>");
	say %results<info>;
  
	# add document
	%results = $session.add("world/World2.xml", "<x>Hello World 2!</x>");
	say %results<info>;
  
	# add document
	%results = $session.add(path => "Universe.xml", input => "<x>Hello Universe!</x>");
	say %results<info>; 
  
	# add document
	%results = $session.add("Universe2.xml", input => "<x>Hello Universe!</x>");
	say %results<info>; 
  
	# run query on database
	say $session.command(command => "xquery collection('database')")<result>;
  
	# drop database
	$session.command(command => "drop db database");
  
	# close session
	$session.close();

	CATCH {
		# print exception
		say $*ERR;
	}
};

