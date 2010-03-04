use IO::Socket;
use Digest::MD5;
use Encode;

package BaseX;
	
	# Konstruktor.
	sub new
	{
		$self = shift;
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
		$tmp = $self->readString;
		$ts = substr($tmp, 0, length($tmp - 1));
		
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
		$sock->send("\0");
		$sock->send($complete);
		$sock->send("\0");
		
		$sock->recv($text, 1);
		if($text != "\0") {
			print "Access denied";
		}
		return $self;
	}
	
	# Executes a command and writes the result to the specified stream.
	sub execute {
		$self = shift;
		$com = shift;
		#$out = shift;
		# send command to server
		$self{sock}->send($com);
		$self{sock}->send("\0");
		$self->init;
		$self{result} = $self->readString;
		$self{info} = $self->readString;
		
		return $self->read == "\0";
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
			$self{bsize} = length($self{sock}->recv($buffer, 4096));
			$self{bpos} = 0;
			@array = split("", $buffer);
		}
		return @array[$self{bpos}];
	}
	
	# Receives a string from the socket.
	sub readString
	{	
		$complete = "";
		while (($d = $self->read) != "\0") {
			$complete = $complete.$d;
			$self{bpos} += 1;
		}
		return $complete;
	}
1;
