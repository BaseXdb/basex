use IO::Socket;
use Digest::MD5;

package Session;
	
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
		$self->{sock} = $sock;
		$self->read;
		
		# receive timestamp
		$ts = substr($self->{text}, 0, length($self->{text} - 1));
		
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
		$sock->send("exit");
	}
	
	# Executes a command.
	sub execute {
		$self = shift;
		$com = shift;
		$out = shift;
	}
	
	# Reads 4096 bytes from the stream.
	sub read
	{
		my $self = shift;
		$self->{sock}->recv($self->{text}, 4096);
	}
	
	# Receives a string from the socket.
	sub readString
	{	
		my $self = shift;
		$self->read;
		$complete = "";
		while (length($self->{text}) > 4095) {
			$complete = $complete.$self->{text};
			$self->read;
		}
		$self->{complete} = $complete;
	}
1;
