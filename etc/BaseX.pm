use IO::Socket;
use Digest::MD5;
use strict;
use warnings;

package BaseX;

our $sock;
our $bpos = 0;
our $bsize = 0;
our $result = "";
our $info = "";
our @array = ();
	
	# Konstruktor.
	sub new
	{
		my $self = shift;
		my $class = {};
		my $host = shift;
		my $port = shift;
		my $user = shift;
		my $pw = shift;
		$sock = new IO::Socket::INET (
			PeerAddr => $host,
			PeerPort => $port,
			Proto => "tcp",);
		die "Can't communicate with the server." unless $sock;

		# receive timestamp
		my $ts = $self->readString;
		
		# code password and timestamp in md5
		my $ctx = Digest::MD5->new;
		$ctx->add($pw);
		my $pwmd5 = $ctx->hexdigest;
		$ctx = Digest::MD5->new;
		my $tmp = $pwmd5.$ts;
		$ctx->add($tmp);
		my $complete = $ctx->hexdigest;
		
		# send username and password
		$sock->send($user);
		$sock->send("\0");
		$sock->send($complete);
		$sock->send("\0");
		
		$sock->recv(my $text, 1);
		if(ord($text) != 0) {
			print "Access denied";
		}
		bless ($class,$self);
		return $self;
	}
	
	# Executes a command and writes the result to the specified stream.
	sub execute {
		my $self = shift;
		my $com = shift;
		#$out = shift;
		# send command to server
		$sock->send($com);
		$sock->send("\0");
		$self->init;
		$result = $self->readString;
		$info = $self->readString;
		
		return ord($self->read) == 0;
	}
	
	# Returns the result.
	sub result {
		return $result;
	}
	
	# Returns the info.
	sub info {
		return $info;	
	}
	
	# Closes the socket.
	sub close {
		$sock->send("exit");
		close($sock);
	}
	
	# Initiates the incoming message.
	sub init {
		$bpos = 0;
		$bsize = 0;	
	}
	
	# Returns the next byte.
	sub read
	{
		#if ($bpos == $bsize) {
		#	$bsize = length($sock->recv(my $buffer, 4096)) - 1;
		#	$bpos = -1;
		#	@array = split("", $buffer);
		#}
		$sock->recv(my $text, 1);
		return $text;
	}
	
	# Receives a string from the socket.
	sub readString
	{	
		my $self = shift;
		my $complete = "";
		while(ord(my $d = $self->read) != 0) {
			$complete = $complete.$d;
		}
		return $complete;
	}
1;
