package org.basex.api.client;

import java.io.*;
import java.net.*;
import java.util.*;

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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class ClientSession extends Session {
  /** Event notifications. */
  private final Map<String, EventNotifier> notifiers =
      Collections.synchronizedMap(new HashMap<String, EventNotifier>());
  /** Server output (buffered). */
  final PrintOutput sout;
  /** Server input. */
  final InputStream sin;

  /** Socket reference. */
  private final Socket socket;
  /** Socket event host. */
  private final String ehost;
  /** Socket event reference. */
  private Socket esocket;

  /**
   * Constructor, specifying login data.
   * @param context database context
   * @param user user name
   * @param pass password
   * @throws IOException I/O exception
   */
  public ClientSession(final Context context, final String user, final String pass)
      throws IOException {
    this(context, user, pass, null);
  }

  /**
   * Constructor, specifying login data and an output stream.
   * @param context database context
   * @param user user name
   * @param pass password
   * @param output client output; if set to {@code null}, results will
   * be returned as strings.
   * @throws IOException I/O exception
   */
  public ClientSession(final Context context, final String user, final String pass,
      final OutputStream output) throws IOException {
    this(context.globalopts.get(GlobalOptions.HOST),
         context.globalopts.get(GlobalOptions.PORT), user, pass, output);
  }

  /**
   * Constructor, specifying the server host:port combination and login data.
   * @param host server name
   * @param port server port
   * @param user user name
   * @param pass password
   * @throws IOException I/O exception
   */
  public ClientSession(final String host, final int port, final String user, final String pass)
      throws IOException {
    this(host, port, user, pass, null);
  }

  /**
   * Constructor, specifying the server host:port combination, login data and
   * an output stream.
   * @param host server name
   * @param port server port
   * @param user user name
   * @param pass password
   * @param output client output; if set to {@code null}, results will
   * be returned as strings.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("resource")
  public ClientSession(final String host, final int port, final String user, final String pass,
      final OutputStream output) throws IOException {

    super(output);
    ehost = host;
    socket = new Socket();
    try {
      // limit timeout to five seconds
      socket.connect(new InetSocketAddress(host, port), 5000);
    } catch(final IllegalArgumentException ex) {
      throw new BaseXException(ex);
    }
    sin = socket.getInputStream();

    // receive timestamp
    final BufferInput bi = new BufferInput(sin);
    final String ts = bi.readString();

    // send user name and hashed password/timestamp
    sout = PrintOutput.get(socket.getOutputStream());
    send(user);
    send(Token.md5(Token.md5(pass) + ts));
    sout.flush();

    // receive success flag
    if(!ok(bi)) throw new LoginException();
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
    if(esocket != null) esocket.close();
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
   * Watches an event.
   * @param name event name
   * @param notifier event notification
   * @throws IOException I/O exception
   */
  public void watch(final String name, final EventNotifier notifier) throws IOException {
    sout.write(ServerCmd.WATCH.code);
    if(esocket == null) {
      sout.flush();
      final BufferInput bi = new BufferInput(sin);
      final int eport = Integer.parseInt(bi.readString());
      // initialize event socket
      esocket = new Socket();
      esocket.connect(new InetSocketAddress(ehost, eport), 5000);
      final OutputStream so = esocket.getOutputStream();
      so.write(bi.readBytes());
      so.write(0);
      so.flush();
      final InputStream is = esocket.getInputStream();
      is.read();
      listen(is);
    }
    send(name);
    sout.flush();
    receive(null);
    notifiers.put(name, notifier);
  }

  /**
   * Unwatches an event.
   * @param name event name
   * @throws IOException I/O exception
   */
  public void unwatch(final String name) throws IOException {
    sout.write(ServerCmd.UNWATCH.code);
    send(name);
    sout.flush();
    receive(null);
    notifiers.remove(name);
  }

  /**
   * Starts the listener thread.
   * @param input input stream
   */
  private void listen(final InputStream input) {
    final BufferInput bi = new BufferInput(input);
    new Thread() {
      @Override
      public void run() {
        try {
          while(true) {
            final EventNotifier n = notifiers.get(bi.readString());
            final String l = bi.readString();
            if(n != null) n.notify(l);
          }
        } catch(final IOException ex) {
          // listener did not receive any more input
        }
      }
    }.start();
  }

  /**
   * Sends the specified stream to the server.
   * @param input input stream
   * @throws IOException I/O exception
   */
  private void send(final InputStream input) throws IOException {
    final EncodingOutput eo = new EncodingOutput(sout);
    for(int b; (b = input.read()) != -1;) eo.write(b);
    sout.write(0);
    sout.flush();
    receive(null);
  }

  /**
   * Receives the info string.
   * @param output output stream to send result to. If {@code null}, no result will be requested
   * @throws IOException I/O exception
   */
  @SuppressWarnings("resource")
  private void receive(final OutputStream output) throws IOException {
    final BufferInput bi = new BufferInput(sin);
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
    final DecodingInput di = new DecodingInput(input);
    for(int b; (b = di.read()) != -1;) output.write(b);
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
  @SuppressWarnings("resource")
  String exec(final ServerCmd command, final String arg, final OutputStream output)
      throws IOException {

    final OutputStream o = output == null ? new ArrayOutput() : output;
    sout.write(command.code);
    send(arg);
    sout.flush();
    final BufferInput bi = new BufferInput(sin);
    receive(bi, o);
    if(!ok(bi)) throw new BaseXException(bi.readString());
    return o.toString();
  }

  @Override
  public String toString() {
     return ehost + ':' + socket.getPort();
  }
}
