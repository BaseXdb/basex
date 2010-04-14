# -----------------------------------------------------------------------------
#
# This Perl module provides methods to connect to and communicate with the
# BaseX Server.
#
# The Constructor of the class expects a hostname, port, username and password
# for the connection. The socket connection will then be established via the
# hostname and the port.
#
# For the execution of commands you need to call the execute() method with the
# database command as argument. The method returns a boolean, indicating if
# the command was successful. The result can be requested with the result()
# method, and the info() method returns additional processing information
# or error output.
#
# -----------------------------------------------------------------------------
#
# Example:
#
# use BaseXClient;
#
# eval {
#   # create session
#   $session = Session->new("localhost", 1984, "admin", "admin");
#
#   # perform command and show result or error output
#   if($session->execute("xquery 1 to 10")) {
#     print $session->result();
#   } else {
#     print $session->info();
#   }
#
#   # close session
#   $session->close();
# };
#
# # print exception
# print $@ if $@;
#
# -----------------------------------------------------------------------------
# (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
# -----------------------------------------------------------------------------

use Digest::MD5;
use IO::Socket;
use warnings;
use strict;

package Session;

# Constructor, creating a new socket connection.
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
  $self->{sock}->send("$user\0$complete\0");

  # evaluate success flag
  return $self if !$self->_read() or die "Access denied.";
}

# Executes the specified command.
sub execute {
  my $self = shift;
  my $cmd = shift;

  # send command to server and receive result
  $self->{sock}->send("$cmd\0");
  $self->{result} = $self->_readString();
  $self->{info} = $self->_readString();
  return !$self->_read();
}

# Returns the result.
sub result {
  my $self = shift;
  return $self->{result};
}

# Returns processing information.
sub info {
  my $self = shift;
  return $self->{info};
}

# Closes the connection.
sub close {
  my $self = shift;
  $self->{sock}->send("exit\0");
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

1;
