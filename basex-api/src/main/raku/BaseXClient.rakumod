# Raku client for BaseX.
# Works with BaseX 7.0 and later
#
# Documentation: https://docs.basex.org/wiki/Clients
# 
# (C) BaseX Team 2005-21, BSD License

use	Digest::MD5;
use	JSON::Fast;

class	Query {
	has	$.session;
	has	@!cache;
	has	$!pos;
	has	$.id;
	has	%.protocol-commands;

	# Post-processes some things after object creation
	submethod	TWEAK(:$session, :$query) {
		%!protocol-commands<bind><clearcache> = 1;
		%!protocol-commands<context><clearcache> = 1;
		for %!protocol-commands.kv -> $cmd, $hash {
			$hash<send>:exists or $hash<send> = [];
			$hash<clearcache>:exists or $hash<clearcache> = 0;
		}

		defined($!session) or die "No session defined!\n";
		defined($query) or die "No query defined";
		$!id = $!session.send-command('query', query => $query)<id>;
	}

	# If a method doesn't exist, it does this instead; this makes the methods defined in %!protocol-commands work
	method	FALLBACK ($name, *%named-params, *@positional-params) {
		# If method isn't in %!protocol-commands either, just die with an error
		%!protocol-commands{$name}:exists or die "Error: Unknown protocol-command '$name'";
		# Run the protocol-command against the server
		my (%rvs) = $!session.send-command($name, QueryObject => self, |%named-params, |@positional-params);
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
	has	$!json;
	has	%!protocol-commands;

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

		# Read protocol information in JSON
		$!json = from-json("BaseXProtocol.json".IO.slurp);
		# Set up session info
		%!protocol-commands = $!json<session>;
	}

	method	query(*%params, *@params) {
		return Query.new(
			session => self, 
			protocol-commands => $!json<query>,
			|%params,
			|@params,
		);
	}

	# If a method doesn't exist, it does this instead; this makes the methods defined in %!protocol-commands work
	method	FALLBACK ($name, *%named-params, *@positional-params) {
		# If method isn't in %!protocol-commands either, just die with an error
		%!protocol-commands{$name}:exists or die "Error: Unknown protocol-command '$name'";
		# Run the protocol-command against the server
		my (%rvs) = self.send-command($name, |%named-params, |@positional-params);
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
		:$QueryObject, 
		*%named-params,
		*@positional-params,
	) {
		# Process $protocol-command -> $sendchar
		my $protocol-command-hash;
		my $type;
		given $protocol-command {
			when %!protocol-commands{$_}:exists {
				$protocol-command-hash = %!protocol-commands{$_};
				$type = 'session';
			}
			when defined($QueryObject) and $QueryObject.protocol-commands{$_}:exists {
				$protocol-command-hash = $QueryObject.protocol-commands{$_};
				$type = 'query';
			}
			die "Don't recognise protocol-command '$protocol-command'\n";
		}
		my $sendchar = $protocol-command-hash<char>:exists ?? chr($protocol-command-hash<char>) !! '';
		defined($sendchar) or die "Unknown character for protocol-command $protocol-command";

		# Process params -> $arg
		my @useparams;
		my $index = 0;
		for $protocol-command-hash<send>.values -> $name {
			{
				when %named-params{$name}:exists {
					defined(%named-params{$name}) or die "Undefined param '$name'";
					push @useparams, map { s:g/(0x00|0xFF)/0xFF($1)/; $_ }, %named-params{$name};
				}
				when @positional-params[$index]:exists {
					push @useparams, map { s:g/(0x00|0xFF)/0xFF($1)/; $_ }, @positional-params[$index];
				}
				push @useparams, '';
			}
			$index++;
		}
		$type eq 'query' and unshift @useparams, $QueryObject.id;
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

