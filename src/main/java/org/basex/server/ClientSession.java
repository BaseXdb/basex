package org.basex.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.basex.core.BaseXException;
import org.basex.core.Command;
import org.basex.core.Commands.Cmd;
import org.basex.core.Context;
import org.basex.core.MainProp;
import org.basex.io.in.BufferInput;
import org.basex.io.out.PrintOutput;
import org.basex.util.Token;

/**
 * This class offers methods to execute database commands via the
 * client/server architecture. Commands are sent to the server instance over
 * a socket connection:
 * <ul>
 * <li> A socket instance is created by the constructor.</li>
 * <li> The {@link #execute} method sends database commands to the server.
 * All strings are encoded as UTF8 and suffixed by a zero byte.</li>
 * <li> If the command has been successfully executed,
 * the result string is read.</li>
 * <li> Next, the command info string is read.</li>
 * <li> A last byte is next sent to indicate if command execution
 * was successful (0) or not (1).</li>
 * <li> {@link #close} closes the session by sending the {@link Cmd#EXIT}
 * command to the server.</li>
 * </ul>
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ClientSession extends Session {
  /** Event notifications. */
  final Map<String, EventNotifier> notifiers =
    Collections.synchronizedMap(new HashMap<String, EventNotifier>());
  /** Server output (buffered). */
  final PrintOutput sout;
  /** Server input. */
  final InputStream sin;

  /** Socket reference. */
  private final Socket socket;
  /** Socket host name. */
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
  public ClientSession(final Context context, final String user,
      final String pass) throws IOException {
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
  public ClientSession(final Context context, final String user,
      final String pass, final OutputStream output) throws IOException {
    this(context.mprop.get(MainProp.HOST), context.mprop.num(MainProp.PORT),
        user, pass, output);
  }

  /**
   * Constructor, specifying the server host:port combination and login data.
   * @param host server name
   * @param port server port
   * @param user user name
   * @param pass password
   * @throws IOException I/O exception
   */
  public ClientSession(final String host, final int port,
      final String user, final String pass) throws IOException {
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
  public ClientSession(final String host, final int port, final String user,
      final String pass, final OutputStream output) throws IOException {

    super(output);
    ehost = host;
    // 5 seconds timeout
    socket = new Socket();
    socket.connect(new InetSocketAddress(host, port), 5000);
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
  public ClientQuery query(final String query) throws IOException {
    return new ClientQuery(query, this);
  }

  @Override
  public void create(final String name, final InputStream input)
      throws IOException {

    sout.write(ServerCmd.CREATE.code);
    send(name);
    send(input);
  }

  @Override
  public void add(final String name, final String target,
      final InputStream input) throws IOException {

    sout.write(ServerCmd.ADD.code);
    send(name);
    send(target);
    send(input);
  }

  @Override
  public void replace(final String path, final InputStream input)
      throws IOException {

    sout.write(ServerCmd.REPLACE.code);
    send(path);
    send(input);
  }

  @Override
  public void store(final String path, final InputStream input)
      throws IOException {

    sout.write(ServerCmd.STORE.code);
    send(path);
    send(input);
  }

  @Override
  public void retrieve(final String path) throws IOException {
    sout.write(ServerCmd.RETRIEVE.code);
    send(path);
    receive(new BufferInput(sin), sout);
  }

  /**
   * Watches an event.
   * @param name event name
   * @param notifier event notification
   * @throws IOException I/O exception
   */
  public void watch(final String name, final EventNotifier notifier)
      throws IOException {

    sout.write(ServerCmd.WATCH.code);
    if(esocket == null) {
      sout.flush();
      final BufferInput bi = new BufferInput(sin);
      final int eport = Integer.parseInt(bi.readString());
      // initialize event socket
      esocket = new Socket();
      esocket.connect(new InetSocketAddress(ehost, eport), 5000);
      final PrintOutput po = PrintOutput.get(esocket.getOutputStream());
      po.print(bi.readString());
      po.write(0);
      po.flush();
      final InputStream is = esocket.getInputStream();
      is.read();
      listen(is);
    }
    send(name);
    final BufferInput bi = new BufferInput(sin);
    info = bi.readString();
    if(!ok(bi)) throw new IOException(info);
    notifiers.put(name, notifier);
  }

  /**
   * Starts the listener thread.
   * @param in input stream
   */
  private void listen(final InputStream in) {
    new Thread() {
      @Override
      public void run() {
        try {
          while(true) {
            final BufferInput bi = new BufferInput(in);
            final EventNotifier n = notifiers.get(bi.readString());
            final String l = bi.readString();
            if(n != null) n.notify(l);
          }
        } catch(final IOException ex) { }
      }
    }.start();
  }

  /**
   * Unwatches an event.
   * @param name event name
   * @throws IOException I/O exception
   */
  public void unwatch(final String name) throws IOException {
    sout.write(ServerCmd.UNWATCH.code);
    send(name);
    final BufferInput bi = new BufferInput(sin);
    info = bi.readString();
    if(!ok(bi)) throw new IOException(info);
    notifiers.remove(name);
  }

  /**
   * Sends the specified stream to the server.
   * @param input input stream
   * @throws IOException I/O exception
   */
  private void send(final InputStream input) throws IOException {
    for(int b; (b = input.read()) != -1;) {
      // 0x00 and 0xFF are prefixed by 0xFF and
      // later decoded in {@link ClientInputStream}
      if(b == 0x00 || b == 0xFF) sout.write(0xFF);
      sout.write(b);
    }
    sout.write(0);
    sout.flush();
    final BufferInput bi = new BufferInput(sin);
    info = bi.readString();
    if(!ok(bi)) throw new IOException(info);
  }

  @Override
  public synchronized void close() throws IOException {
    if(esocket != null) esocket.close();
    socket.close();
  }

  /**
   * Sends a string to the server.
   * @param s string to be sent
   * @throws IOException I/O exception
   */
  void send(final String s) throws IOException {
    sout.print(s);
    sout.write(0);
    sout.flush();
  }

  /**
   * Checks the next success flag.
   * @param bi buffer input
   * @return value of check
   * @throws IOException I/O exception
   */
  boolean ok(final BufferInput bi) throws IOException {
    return bi.read() <= 0;
  }

  /**
   * Retrieves data from the server.
   * @param bi buffered server input
   * @param os output stream
   * @throws IOException I/O exception
   */
  void receive(final BufferInput bi, final OutputStream os)
      throws IOException {
    for(int b; (b = bi.read()) > 0;) os.write(b == 0xFF ? bi.read() : b);
  }
  
  @Override
  protected void execute(final String cmd, final OutputStream os)
      throws IOException {

    send(cmd);
    final BufferInput bi = new BufferInput(sin);
    receive(bi, os);
    info = bi.readString();
    if(!ok(bi)) throw new BaseXException(info);
  }

  @Override
  protected void execute(final Command cmd, final OutputStream os)
      throws IOException {
    execute(cmd.toString(), os);
  }
}
