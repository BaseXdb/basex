# Language Binding for BaseX.
# Works with BaseX 6.3.1 and later
# Documentation: http://basex.org/api
# 
# (C) BaseX Team 2005-11, ISC License

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
  my $ts = $self->_receive();

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
  $self->send("$cmd");
  $self->{result} = $self->_receive();
  $self->{info} = $self->_receive();
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
  $self->send("exit");
  close($self->{sock});
}

# Receives a string from the socket.
sub _receive {
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
  $id = execu("\0", $cmd);
  return $self;
}

# see readme.txt
sub init {
  return execu("\4", $id);
}

# see readme.txt
sub bind {
  shift;
  my $name = shift;
  my $value = shift;
  execu("\3", "$id\0$name\0$value\0");
}

# see readme.txt
sub more {
  $next = execu("\1", $id);
  return $next;
}

# see readme.txt
sub next {
  return $next;
}

# see readme.txt
sub execute {
  return execu("\5", $id);
}

# see readme.txt
sub info {
  return execu("\6", $id);
}

# see readme.txt
sub close {
  return execu("\2", $id);
}

# see readme.txt
sub execu {
  my $cmd = shift;
  my $arg = shift;
  $session->send("$cmd$arg");
  my $s = $session->_receive();
  if (!$session->ok()) {
    die $session->_receive();
  }
  return $s;
}

1;