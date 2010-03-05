# This Perl module provides all methods to connect and communicate with the
# BaseX Server.
#
# The Constructor of the class expects a hostname, port, username and
# password for the connection. The socket connection will then be established
# via the hostname and the port.
#
# For the execution of commands you need to call the execute method with the command
# as argument. The result and the info will then be written to the corresponding string.
# These strings can be fetched with the methods result() and info().
# 
# Example:
#
# use BaseX;
#
# $session = new BaseX("localhost", 1984, "admin", "admin");
#  if($session->execute("xquery 1 + 2")) {
#    print $session->result();
#  } else {
#    print $session->info();
#  }
#  $session->close();
#
# (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License

use IO::Socket;
use Digest::MD5;
use strict;
use warnings;

package BaseX;
	
	# Constructor.
	sub new {
		my $class = shift;
		my $self = bless({}, $class);
		my $host = shift;
		my $port = shift;
		my $user = shift;
		my $pw = shift;
		
		# create socket
		$self->{sock} = new IO::Socket::INET (
			PeerAddr => $host,
			PeerPort => $port,
			Proto => "tcp",);
		die "Can't communicate with the server." unless $self->{sock};
		
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
		$self->{sock}->send($user);
		$self->{sock}->send("\0");
		$self->{sock}->send($complete);
		$self->{sock}->send("\0");
		
		$self->{sock}->recv(my $text, 1);
		if(ord($text) != 0) {
			print "Access denied";
		}
		return $self; 
	}
	
	# Executes a command and writes the result and info to the corresponding strings.
	sub execute {
		my $self = shift;
		my $com = shift;
		# send command to server
		$self->{sock}->send($com);
		$self->{sock}->send("\0");
		$self->{result} = $self->readString;
		$self->{info} = $self->readString;
		
		return ord($self->read) == 0;
	}
	
	# Returns the result.
	sub result {
		my $self = shift;
		return $self->{result};
	}
	
	# Returns the info.
	sub info {
		my $self = shift;
		return $self->{info};
	}
	
	# Closes the socket.
	sub close {
		my $self = shift;
		$self->{sock}->send("exit");
		close($self->{sock});
	}
	
	# Returns the next byte.
	sub read {
		my $self = shift;
		$self->{sock}->recv(my $text, 1);
		return $text;
	}
	
	# Receives a string from the socket.
	sub readString {
		my $self = shift;
		my $complete = "";
		while(ord(my $d = $self->read) != 0) {
			$complete = $complete.$d;
		}
		return $complete;
	}	
1;
