# Perl client for BaseX.
# Works with BaseX 13.0 and later
#
# Documentation: https://docs.basex.org/wiki/Clients
#
# (C) BaseX Team, BSD License

use Digest::SHA;
use IO::Socket;
use warnings;
use strict;

package Session;

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

  # receive challenge: {realm}:{nonce}
  my $nonce = (split(':', $self->_receive()))[1];

  # request the password parameters: send username and an empty hash
  $self->send($user);
  $self->send("");

  # receive password parameters: {algorithm}:{salt}
  my $salt = (split(':', $self->_receive()))[1];

  # send hashed password
  my $code = Digest::SHA->new(256)->add($salt.$pw)->hexdigest();
  $self->send(Digest::SHA->new(256)->add($code.$nonce)->hexdigest());

  # evaluate success flag
  return $self if !$self->_read() or die "Access denied.";
}

sub execute {
  my $self = shift;
  my $cmd = shift;

  # send command to server and receive result
  $self->send($cmd);
  $self->{result} = $self->_receive();
  $self->{info} = $self->_receive();
  die $self->{info} if !$self->ok();
  return $self->{result};
}

sub query {
  return Query->new(shift, shift);
}

sub create {
  shift->sendInput(8, shift, shift);
}

sub add {
  shift->sendInput(9, shift, shift);
}

sub put {
  shift->sendInput(12, shift, shift);
}

sub put_binary {
  shift->sendInput(13, shift, shift);
}

sub info {
  return shift->{info};
}

sub close {
  my $self = shift;
  $self->send("exit");
  close($self->{sock});
}

# Receives a string from the socket.
sub _receive {
  my $self = shift;
  my $data = "";
  while($self->_read()) {
    $self->_read() if ord($_) == 255;
    $data .= $_;
  }
  return $data;
}

# Returns a single byte from the socket.
sub _read {
  shift->{sock}->recv($_, 1);
  return ord();
}

# Returns success check.
sub ok {
  return !shift->_read();
}

# Sends the specified string.
sub send {
  shift->{sock}->send((shift).chr(0));
}

# Sends the specified input.
sub sendInput {
  my $self = shift;
  my $code = shift;
  my $str = shift;
  my $input = shift;

  # prefix 0x00 and 0xFF bytes with 0xFF
  $input =~ s/([\x00\xFF])/\xFF$1/g;
  $self->send(chr($code).$str);
  $self->send($input);

  $self->{info} = $self->_receive();
  die $self->{info} if !$self->ok();
}

1;

package Query;

sub new {
  my $class = shift;
  my $self = bless({ session => shift, cache => [], pos => 0 }, $class);
  $self->{id} = $self->exc(chr(0), shift);
  return $self;
}

sub bind {
  my $self = shift;
  my $name = shift;
  my $value = shift;
  my $type = shift;
  $type = "" if !$type;
  $self->exc(chr(3), $self->{id}.chr(0).$name.chr(0).$value.chr(0).$type);
  $self->{cache} = [];
}

sub context {
  my $self = shift;
  my $value = shift;
  my $type = shift;
  $type = "" if !$type;
  $self->exc(chr(14), $self->{id}.chr(0).$value.chr(0).$type);
  $self->{cache} = [];
}

sub execute {
  my $self = shift;
  return $self->exc(chr(5), $self->{id});
}

sub more {
  my $self = shift;
  my $session = $self->{session};
  if(!@{$self->{cache}}) {
    $session->send(chr(4).$self->{id});
    push(@{$self->{cache}}, $session->_receive()) while $session->_read();
    die $session->_receive() if !$session->ok();
    $self->{pos} = 0;
  }
  my $more = $self->{pos} < @{$self->{cache}};
  $self->{cache} = [] if !$more;
  return $more;
}

sub next {
  my $self = shift;
  return $self->more() && $self->{cache}[$self->{pos}++];
}

sub info {
  my $self = shift;
  return $self->exc(chr(6), $self->{id});
}

sub options {
  my $self = shift;
  return $self->exc(chr(7), $self->{id});
}

sub close {
  my $self = shift;
  $self->exc(chr(2), $self->{id});
}

sub exc {
  my $self = shift;
  my $cmd = shift;
  my $arg = shift;
  my $session = $self->{session};
  $session->send($cmd.$arg);
  my $s = $session->_receive();
  die $session->_receive() if !$session->ok();
  return $s;
}

1;
