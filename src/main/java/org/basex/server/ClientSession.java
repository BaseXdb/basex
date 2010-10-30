package org.basex.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
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
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class ClientSession extends Session {
  /** Socket reference. */
  final Socket socket;
  /** Server output. */
  final PrintOutput sout;
  /** Server input. */
  final InputStream sin;

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
