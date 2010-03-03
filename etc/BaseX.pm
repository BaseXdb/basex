use IO::Socket;
use Digest::MD5;

package BaseX;
	
	# Konstruktor.
	sub new
	{
		my $self = shift;
		$host = shift;
		$port = shift;
		$user = shift;
		$pw = shift;
		$sock = new IO::Socket::INET (
			PeerAddr => $host,
			PeerPort => $port,
			Proto => "tcp",);
		die "Can't communicate with the server." unless $sock;
		$self{sock} = $sock;

		# receive timestamp
		$ts = substr($self->read, 0, length($self->{text} - 1));
		
		# code password and timestamp in md5
		$ctx = Digest::MD5->new;
		$ctx->add($pw);
		$pwmd5 = $ctx->hexdigest;
		$ctx = Digest::MD5->new;
		$tmp = $pwmd5.$ts;
		$ctx->add($tmp);
		$complete = $ctx->hexdigest;
		
		# send username and password
		$sock->send($user);
		$sock->send("\x00");
		$sock->send($complete);
		$sock->send("\x00");
		
		$sock->recv($text, 1);
		if($text != "\x00") {
			print "Access denied";
		}
	}
	
	# Executes a command and writes the result to the specified stream.
	sub execute {
		$self = shift;
		$com = shift;
		#$out = shift;
		# send command to server
		$self{sock}->send($com);
		$self{sock}->send("\x00");
		$self->init;
		$self{result} = $self->readString;
		$self{info} = $self->readString;
		
		return $self->read == "\x00";
	}
	
	# Returns the result.
	sub result {
		return $self{result};
	}
	
	# Returns the info.
	sub info {
		return $self{info};	
	}
	
	# Closes the socket.
	sub close {
		close($self->{sock});
	}
	
	# Initiates the incoming message.
	sub init {
		$self{bpos} = 0;
		$self{bsize} = 0;	
	}
	
	# Returns the next byte.
	sub read
	{
		if ($self{bpos} == $self{bsize}) {
			$self{bsize} = length($self{sock}->recv($self{$buffer}, 4096));
			$self{bpos} = 0;
		}
		return $self{buffer}[$self{bpos}++];
	}
	
	# Receives a string from the socket.
	sub readString
	{	
		my $self = shift;
		$complete = "";
		while ($d = $self->read != "\x00") {
			$complete = $complete.$d;
			$self->read;
		}
		return $complete;
	}
1;
