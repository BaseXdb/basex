# Raku client for BaseX.
# Works with BaseX 7.0 and later
#
# Documentation: https://docs.basex.org/wiki/Clients
# 
# (C) BaseX Team 2005-21, BSD License

#use	OpenSSL::Digest::MD5;
use	Digest::MD5;

class	Query {
	has	$.session;
	has	@!cache;
	has	$!pos;
	has	$.id;
	has	%.protocol-commands = (
		close => {
			char => 0x02,
			receive => ['bool'],
		},
		bind => {
			char => 0x03,
			send => [<name value type>],
			receive => ['NULL'],
			clearcache => 1,
		},
		execute => {
			char => 0x05,
			receive => [<result>],
		},
		info => {
			char => 0x06,
			receive => [<result>],
		},
		options => {
			char => 0x07,
			receive => [<result>],
		},
		context => {
			char => 0x0E,
			send => [<value type>],
			clearcache => 1,
		},
	);

	# Post-processes some things after object creation
	submethod	TWEAK(:$session, :$command) {
		for %!protocol-commands.kv -> $cmd, $hash {
			$hash<send>:exists or $hash<send> = [];
			$hash<clearcache>:exists or $hash<clearcache> = 0;
		}

		defined($!session) or die "No session defined!\n";
		$!id = $!session.send-command('query', $command)<id>;
	}

	# If a method doesn't exist, it does this instead; this makes the methods defined in %!protocol-commands work
	method	FALLBACK ($name, *@params) {
		# If method isn't in %!protocol-commands either, just die with an error
		%!protocol-commands{$name}:exists or die "Error: Unknown protocol-command '$name'";
		# Run the protocol-command against the server
		my (%rvs) = $!session.send-command($name, query => self, |@params);
		# Clear the cache if necessary
		%!protocol-commands{$name}<clearcache> and @!cache = ();
		# Return whatever came back from the server
		return %rvs;
	}

	method	next {
		return self.more() && @!cache[$!pos++];
	}

	method	more {
		if (!@!cache) {
			$!session.send(chr(4) ~ $!id ~ chr(0));
			push(@!cache, $!session.receive()) while $!session.read();
			die $!session.receive() if !$!session.ok();
			$!pos = 0;
		}
		my $more = $!pos < @!cache;
		@!cache = () if !$more;
		return $more;
	}
}

class	Session {
	has	IO::Socket::INET $!sock;
	has	Cool		 $!buf;
	has	$!result;
	has	$.info;
	has	%!protocol-commands = (
		command => {
			send => [<command>],
			receive => [<result info>],
		},
		query => { # creates a query
			char => 0x00,
			send => [<query>],
			receive => [<id>],
		},
		create => {
			char => 0x08,
			send => [<name input>],
			receive => [<info>],
		},
		add => {
			char => 0x09,
			send => [<path input>],
			receive => [<info>],
		},
		replace => {
			char => 0x0C,
			send => [<path input>],
			receive => [<info>],
		},
		store => {
			char => 0x0D,
			send => [<path input>],
			receive => [<info>],
		},
	);


	submethod	TWEAK(:$host, :$port, :$username, :$password) {
		# create server connection
		$!sock = IO::Socket::INET.new(:$host, :$port) or die "Can't communicate with the server.";

		# receive server response
		my $code; my $nonce;
		my @response = split(':', self.receive());
		if (@response > 1) {
			# support for digest authentication
			$code = "$username:@response[0]:$password";
			$nonce = @response[1];
		} else {
			# support for cram-md5 (Version < 8.0)
			$code = $password;
			$nonce = @response[0];
		}

		# send username and hashed password/timestamp
		my $codemd5  = Digest::MD5.new().md5_hex($code);
		my $complete = Digest::MD5.new().md5_hex($codemd5 ~ $nonce);
		self.send($username ~ chr(0) ~ $complete);

		# evaluate success flag
		self.read() and die "Access denied.";
	}

	method	query(*%params) {
		return Query.new(session => self, |%params);
	}

	# If a method doesn't exist, it does this instead; this makes the methods defined in %!protocol-commands work
	method	FALLBACK ($name, *@params) {
		# If method isn't in %!protocol-commands either, just die with an error
		%!protocol-commands{$name}:exists or die "Error: Unknown protocol-command '$name'";
		# Run the protocol-command against the server
		my (%rvs) = self.send-command($name, |@params);
		# Return whatever came back from the server
		return %rvs;
	}


	method	close {
		self.send("exit");
		$!sock.close();
	}

	# Receives a string from the socket.
	method	receive {
		my $data = '';
		while (self.read()) {
			self.read() if ord($!buf) == 255; # Deal with 0x00 and 0xFF
			$data ~= $!buf;
		}
#		say " <= " ~ $data.raku;
		return $data;
	}

	# Returns a single byte from the socket.
	method	read {
		$!buf = $!sock.recv(1);
		return ord($!buf);
	}

	# Returns success check.
	method	ok {
		return !self.read();
	}

	# Sends the specified string.
	method	send($sendstr) {
#		say " => " ~ $sendstr.raku;
		$!sock.print($sendstr ~ chr(0));
	}

	method	send-command($protocol-command, 
		:$query, 
		*@params
	) {
		# Process $protocol-command -> $sendchar
		my $protocol-command-hash;
		my $type;
		given $protocol-command {
			when %!protocol-commands{$_}:exists {
				$protocol-command-hash = %!protocol-commands{$_};
				$type = 'session';
			}
			when defined($query) and $query.protocol-commands{$_}:exists {
				$protocol-command-hash = $query.protocol-commands{$_};
				$type = 'query';
			}
			die "Don't recognise protocol-command '$protocol-command'\n";
		}
		my $sendchar = $protocol-command-hash<char>:exists ?? chr($protocol-command-hash<char>) !! '';
		defined($sendchar) or die "Unknown character for protocol-command $protocol-command";

		# Process params -> $arg
		my @useparams = map { s:g/(0x00|0xFF)/0xFF($1)/; $_ }, @params; # clean data
		while @useparams < $protocol-command-hash<send>.elems { push @useparams, ''; }
		$type eq 'query' and unshift @useparams, $query.id;
		my $arg = join chr(0), @useparams;
		defined($arg) or die "Unknown argument in protocol-command '$protocol-command'";

		# Send/receive
		self.send($sendchar ~ $arg);
		$protocol-command-hash<receive>:exists or die "receive parameters not defined for protocol-command '$protocol-command'";
		my %retvals;
		for $protocol-command-hash<receive>.values -> $param {
			$param eq 'NULL' and do {
				my $t = self.ok() or die "Incorrectly terminated NULL ($t)";
				next;
			};
			%retvals{$param} = self.receive();
		}
		self.ok() or die self.receive();
		return %retvals;
	}
}

