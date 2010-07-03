package org.basex.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.basex.core.BaseXException;
import org.basex.core.Session;
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
  private final Socket socket;
  /** Output stream. */
  private final PrintOutput out;
  /** Input stream. */
  private final InputStream in;

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
    this(context.prop.get(Prop.HOST), context.prop.num(Prop.PORT), user, pw);
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

    socket = new Socket();
    socket.connect(new InetSocketAddress(host, port), 5000);
    in = socket.getInputStream();

    // receive timestamp
    final String ts = new BufferInput(in).readString();

    // send user name and hashed password/timestamp
    out = new PrintOutput(socket.getOutputStream());
    send(user);
    send(Token.md5(Token.md5(pw) + ts));
    out.flush();

    // receive success flag
    if(in.read() != 0) throw new LoginException();
  }

  @Override
  public void execute(final String cmd, final OutputStream o)
      throws BaseXException {

    try {
      send(cmd);
      final BufferInput bi = new BufferInput(in);
      int l;
      while((l = bi.read()) != 0) o.write(l);
      final String inf = bi.readString();
      if(bi.read() != 0) throw new BaseXException(inf);
      info = inf;
    } catch(final IOException ex) {
      throw new BaseXException(ex.getMessage());
    }
  }

  @Override
  public void execute(final Command cmd, final OutputStream o)
      throws BaseXException {
    execute(cmd.toString(), o);
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
  private void send(final String s) throws IOException {
    out.print(s);
    out.write(0);
  }
}
