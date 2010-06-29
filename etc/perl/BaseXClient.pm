# -----------------------------------------------------------------------------
#
# This module provides methods to connect to and communicate with the
# BaseX Server.
#
# The Constructor of the class expects a hostname, port, username and password
# for the connection. The socket connection will then be established via the
# hostname and the port.
#
# For the execution of commands you need to call the execute() method with the
# database command as argument. The method returns the result or throws
# an exception with the received error message.
# For the execution of the iterative version of a query you need to call
# the query() method. The results will then be returned via the more() and
# the next() methods. If an error occurs an exception will be thrown.
#
# -----------------------------------------------------------------------------
# (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
# -----------------------------------------------------------------------------

use Digest::MD5;
use IO::Socket;
use warnings;
use strict;

package Session;

# see readme.txt
sub new {
  my $class = shift;
  my $host = shift;
  my $port = shift;
  my $user = shift;
  my $pw = shift;
  my $self = bless({}, $class);

  # create server connection
  $self->{sock} = IO::Socket::INET->new(
    PeerAddr => $host, PeerPort => $port, Proto => "tcp") or
    die "Can't communicate with the server.";

  # receive timestamp
  my $ts = $self->_readString();

  # send username and hashed password/timestamp
  my $pwmd5 = Digest::MD5->new()->add($pw)->hexdigest();
  my $complete = Digest::MD5->new()->add($pwmd5.$ts)->hexdigest();
  $self->send("$user\0$complete");

  # evaluate success flag
  return $self if !$self->_read() or die "Access denied.";
}

# see readme.txt
sub execute {
  my $self = shift;
  my $cmd = shift;

  # send command to server and receive result
  $self->send("$cmd\0");
  $self->{result} = $self->_readString();
  $self->{info} = $self->_readString();
  if (!$self->ok()) {
    die $self->{info};
  }
  return $self->{result};
}

# see readme.txt
sub query {
  my $self = shift;
  my $cmd = shift;
  return Query->new($self, $cmd);
}

# see readme.txt
sub info {
  my $self = shift;
  return $self->{info};
}

# see readme.txt
sub close {
  my $self = shift;
  $self->send("exit\0");
  close($self->{sock});
}

# Receives a string from the socket.
sub _readString {
  my $self = shift;
  my $text = "";
  $text .= $_ while $self->_read();
  return $text;
}

# Returns a single byte from the socket.
sub _read {
  my $self = shift;
  $self->{sock}->recv($_, 1);
  return ord();
}

# Returns success check.
sub ok {
  my $self = shift;
  return !$self->_read();
}

# Sends the defined str.
sub send {
  my $self = shift;
  my $str = shift;
  $self->{sock}->send("$str\0");
}

1;


package Query;

our $session;
our $id;
our $next;

# see readme.txt
sub new {
  my $class = shift;
  $session = shift;
  my $cmd = shift;
  my $self = bless({}, $class);
  $session->send("\0$cmd");
  $id = $session->_readString();
  if (!$session->ok()) {
    die $session->_readString();
  }
  return $self;
}

# see readme.txt
sub more {
  $session->send("\1$id");
  $next = $session->_readString();
  if (!$session->ok()) {
    die $session->_readString();
  }
  return length($next) != 0;
}

# see readme.txt
sub next {
  return $next;
}

# see readme.txt
sub close {
  $session->send("\2$id");
}

1;