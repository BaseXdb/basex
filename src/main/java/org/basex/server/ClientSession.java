package org.basex.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.Commands.Cmd;
import org.basex.io.BufferInput;
import org.basex.io.PrintOutput;
import org.basex.util.Token;

/**
 * This wrapper sends commands to the server instance over a socket
 * connection. It extends the {@link Session} class:
 *
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
  /** Socket reference. */
  final Socket socket;
  /** Server output. */
  final PrintOutput sout;
  /** Server input. */
  final InputStream sin;
  /** Event notifications. */
  Map<String, EventNotification> en;
  /** Socket event reference. */
  Socket esocket;
  /** Socket host name. */
  String ehost;

  /**
   * Constructor, specifying the database context and the
   * login and password.
   * @param context database context
   * @param user user name
   * @param pw password
   * @throws IOException I/O exception
   */
  public ClientSession(final Context context, final String user,
      final String pw) throws IOException {
    this(context, user, pw, null);
  }

  /**
   * Constructor, specifying the database context and the
   * login and password.
   * @param context database context
   * @param user user name
   * @param pw password
   * @param output client output; if set to {@code null}, results will
   * be returned as strings.
   * @throws IOException I/O exception
   */
  public ClientSession(final Context context, final String user,
      final String pw, final OutputStream output) throws IOException {
    this(context.prop.get(Prop.HOST), context.prop.num(Prop.PORT),
        user, pw, output);
  }

  /**
   * Constructor, specifying the server host:port combination and the
   * login and password.
   * @param host server name
   * @param port server port
   * @param user user name
   * @param pw password
   * @throws IOException I/O exception
   */
  public ClientSession(final String host, final int port,
      final String user, final String pw) throws IOException {
    this(host, port, user, pw, null);
  }

  /**
   * Constructor, specifying the server host:port combination and the
   * login and password.
   * @param host server name
   * @param port server port
   * @param user user name
   * @param pw password
   * @param output client output; if set to {@code null}, results will
   * be returned as strings.
   * @throws IOException I/O exception
   */
  public ClientSession(final String host, final int port, final String user,
      final String pw, final OutputStream output) throws IOException {

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
    send(Token.md5(Token.md5(pw) + ts));
    sout.flush();

    // receive success flag
    if(!ok(bi)) throw new LoginException();
  }

  @Override
  public void create(final String name, final InputStream input)
      throws BaseXException {
    try {
      sout.write(8);
      send(name);
      send(input);
    } catch(final IOException ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  public void add(final String name, final String target,
      final InputStream input) throws BaseXException {
    try {
      sout.write(9);
      send(name);
      send(target);
      send(input);
    } catch(final IOException ex) {
      throw new BaseXException(ex);
    }
  }

  /**
   * Watches an event.
   * @param name event name
   * @param notification event notification
   * @throws BaseXException exception
   */
  public void watch(final String name,
      final EventNotification notification) throws BaseXException {
    try {
      sout.write(10);
      send(name);
      final BufferInput bi = new BufferInput(sin);
      if(esocket == null) {
        String id = bi.readString();
        int eport = new Integer(bi.readString()).intValue();
        // initialize event notifications
        this.en = new HashMap<String, EventNotification>();
        esocket = new Socket();
        esocket.connect(new InetSocketAddress(ehost, eport), 5000);
        PrintOutput tout = PrintOutput.get(esocket.getOutputStream());
        tout.print(id);
        tout.write(0);
        tout.flush();
        startListener(esocket.getInputStream());
      }
      info = bi.readString();
      if(!ok(bi)) throw new IOException(info);
      en.put(name, notification);
    } catch(IOException e) {
      throw new BaseXException(e);
    }
  }

  /**
   * Starts the listener thread.
   * @param in input stream
   */
  private void startListener(final InputStream in) {
    new Thread() {
      @Override
      public void run() {
        try {
          while(true) {
            BufferInput bi = new BufferInput(in);
            String name = bi.readString();
            String val = bi.readString();
            en.get(name).update(val);
          }
        } catch(Exception e) { }
       }
    }.start();
  }

  /**
   * Unwatch an event.
   * @param name event name
   * @throws BaseXException exception
   */
  public void unwatch(final String name) throws BaseXException {
    try {
      sout.write(11);
      send(name);
      final BufferInput bi = new BufferInput(sin);
      info = bi.readString();
      if(!ok(bi)) throw new IOException(info);
      en.remove(name);
    } catch(IOException e) {
      throw new BaseXException(e);
    }
  }

  /**
   * Executes queries q1 and q2 and sends result of query q2 to other clients.
   * @param name event name
   * @param q1 query string
   * @param q2 query string
   * @throws BaseXException exception
   */
  public void event(final String name, final String q1,
      final String q2) throws BaseXException {
    if(q2 != null) {
      execute("xquery db:event(" + name + ", " + q1 + ", " + q2 + ")");
    } else {
      execute("xquery db:event(" + name + ", " + q1 + ")");
    }
  }

  /**
   * Executes a query and sends result of it to other clients.
   * @param name event name
   * @param q1 query to execute
   * @throws BaseXException exception
   */
  public void event(final String name, final String q1) throws BaseXException {
    event(name, q1, null);
  }

  /**
   * Sends the specified stream to the server.
   * @param input input stream
   * @throws IOException I/O exception
   */
  private void send(final InputStream input) throws IOException {
    int l;
    while((l = input.read()) != -1) sout.write(l);
    sout.write(0);
    sout.flush();
    final BufferInput bi = new BufferInput(sin);
    info = bi.readString();
    if(!ok(bi)) throw new IOException(info);
  }

  @Override
  public ClientQuery query(final String query) throws BaseXException {
    return new ClientQuery(query, this);
  }

  @Override
  public void close() throws IOException {
    send(Cmd.EXIT.toString());
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
    return bi.read() == 0;
  }

  @Override
  protected void execute(final String cmd, final OutputStream os)
      throws BaseXException {

    try {
      send(cmd);
      final BufferInput bi = new BufferInput(sin);
      int l;
      while((l = bi.read()) != 0) os.write(l);
      info = bi.readString();
      if(!ok(bi)) throw new BaseXException(info);
    } catch(final IOException ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  protected void execute(final Command cmd, final OutputStream os)
      throws BaseXException {
    execute(cmd.toString(), os);
  }
}