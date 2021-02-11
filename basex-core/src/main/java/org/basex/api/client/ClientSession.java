package org.basex.api.client;

import java.io.*;
import java.net.*;

import org.basex.core.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * This class contains methods to execute database commands via the client/server architecture.
 * Commands are sent to the server instance over a socket connection:
 * <ul>
 * <li> A socket instance is created by the constructor.</li>
 * <li> The {@link #execute} method sends database commands to the server.
 * All strings are encoded as UTF8 and suffixed by a zero byte.</li>
 * <li> If the command has been successfully executed, the result string is read.</li>
 * <li> Next, the command info string is read.</li>
 * <li> A last byte is next sent to indicate if command execution
 * was successful (0) or not (1).</li>
 * <li> {@link #close} closes the session by sending the {@link Cmd#EXIT}
 * command to the server.</li>
 * </ul>
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class ClientSession extends Session {
  /** Server output (buffered). */
  final PrintOutput sout;
  /** Server input. */
  final InputStream sin;

  /** Socket reference. */
  private final Socket socket;

  /**
   * Constructor, specifying login data.
   * @param context database context
   * @param username user name
   * @param password password (plain text)
   * @throws IOException I/O exception
   */
  public ClientSession(final Context context, final String username, final String password)
      throws IOException {
    this(context, username, password, null);
  }

  /**
   * Constructor, specifying login data and an output stream.
   * @param context database context
   * @param username user name
   * @param password password (plain text)
   * @param output client output; if set to {@code null}, results will be returned as strings
   * @throws IOException I/O exception
   */
  public ClientSession(final Context context, final String username, final String password,
      final OutputStream output) throws IOException {
    this(context.soptions.get(StaticOptions.HOST),
         context.soptions.get(StaticOptions.PORT), username, password, output);
  }

  /**
   * Constructor, specifying the server host:port combination and login data.
   * @param host server name
   * @param port server port
   * @param username user name
   * @param password password (plain text)
   * @throws IOException I/O exception
   */
  public ClientSession(final String host, final int port, final String username,
      final String password) throws IOException {
    this(host, port, username, password, null);
  }

  /**
   * Constructor, specifying the server host:port combination, login data and an output stream.
   * @param host server name
   * @param port server port
   * @param username user name
   * @param password password (plain text)
   * @param output client output; if set to {@code null}, results will be returned as strings
   * @throws IOException I/O exception
   */
  public ClientSession(final String host, final int port, final String username,
      final String password, final OutputStream output) throws IOException {

    super(output);
    socket = new Socket();
    socket.setTcpNoDelay(true);
    try {
      // limit timeout to five seconds
      socket.connect(new InetSocketAddress(host, port), 5000);
    } catch(final IllegalArgumentException ex) {
      throw new BaseXException(ex);
    }
    sin = socket.getInputStream();

    // receive server response
    final BufferInput bi = BufferInput.get(sin);
    final String[] response = Strings.split(bi.readString(), ':');
    final String code, nonce;
    if(response.length > 1) {
      // support for digest authentication
      code = username + ':' + response[0] + ':' + password;
      nonce = response[1];
    } else {
      // support for cram-md5 (Version < 8.0)
      code = password;
      nonce = response[0];
    }

    // send user name and hashed password
    sout = PrintOutput.get(socket.getOutputStream());
    send(username);
    send(Strings.md5(Strings.md5(code) + nonce));
    sout.flush();

    // receive success flag
    if(!ok(bi)) throw new LoginException(username);
  }

  @Override
  public void create(final String name, final InputStream input) throws IOException {
    send(ServerCmd.CREATE, input, name);
  }

  @Override
  public void add(final String path, final InputStream input) throws IOException {
    send(ServerCmd.ADD, input, path);
  }

  @Override
  public void replace(final String path, final InputStream input) throws IOException {
    send(ServerCmd.REPLACE, input, path);
  }

  @Override
  public void store(final String path, final InputStream input) throws IOException {
    send(ServerCmd.STORE, input, path);
  }

  @Override
  public ClientQuery query(final String query) throws IOException {
    return new ClientQuery(query, this, out);
  }

  @Override
  public synchronized void close() throws IOException {
    socket.close();
  }

  @Override
  protected void execute(final String command, final OutputStream output) throws IOException {
    send(command);
    sout.flush();
    receive(output);
  }

  @Override
  protected void execute(final Command command, final OutputStream output) throws IOException {
    execute(command.toString(), output);
  }

  /**
   * Sends the specified stream to the server.
   * @param input input stream
   * @throws IOException I/O exception
   */
  private void send(final InputStream input) throws IOException {
    final ServerOutput so = new ServerOutput(sout);
    for(int b; (b = input.read()) != -1;) so.write(b);
    sout.write(0);
    sout.flush();
    receive(null);
  }

  /**
   * Receives the info string.
   * @param output output stream to send result to (if {@code null}, no result will be requested)
   * @throws IOException I/O exception
   */
  private void receive(final OutputStream output) throws IOException {
    final BufferInput bi = BufferInput.get(sin);
    if(output != null) receive(bi, output);
    info = bi.readString();
    if(!ok(bi)) throw new BaseXException(info);
  }

  /**
   * Checks the next success flag.
   * @param input buffer input
   * @return value of check
   * @throws IOException I/O exception
   */
  static boolean ok(final BufferInput input) throws IOException {
    return input.read() == 0;
  }

  /**
   * Sends the specified command, string arguments and input.
   * @param command command
   * @param input input stream
   * @param args string arguments
   * @throws IOException I/O exception
   */
  private void send(final ServerCmd command, final InputStream input, final String... args)
      throws IOException {

    sout.write(command.code);
    for(final String arg : args) send(arg);
    send(input);
  }

  /**
   * Retrieves data from the server.
   * @param input buffered server input
   * @param output output stream
   * @throws IOException I/O exception
   */
  static void receive(final BufferInput input, final OutputStream output) throws IOException {
    final ServerInput si = new ServerInput(input);
    for(int b; (b = si.read()) != -1;) output.write(b);
  }

  /**
   * Sends a string to the server.
   * @param string string to be sent
   * @throws IOException I/O exception
   */
  void send(final String string) throws IOException {
    sout.write(Token.token(string));
    sout.write(0);
  }

  /**
   * Executes a command and sends the result to the specified output stream.
   * @param command server command
   * @param arg argument
   * @param output target output stream
   * @return string
   * @throws IOException I/O exception
   */
  String exec(final ServerCmd command, final String arg, final OutputStream output)
      throws IOException {

    final OutputStream o = output == null ? new ArrayOutput() : output;
    sout.write(command.code);
    send(arg);
    sout.flush();
    final BufferInput bi = BufferInput.get(sin);
    receive(bi, o);
    if(!ok(bi)) throw new BaseXException(bi.readString());
    return o.toString();
  }

  @Override
  public String toString() {
    return Prop.PROJECT + ":/" + socket.getLocalAddress() + ':' + socket.getPort();
  }
}
