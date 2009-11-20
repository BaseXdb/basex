package org.basex.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.basex.core.Session;
import org.basex.core.Context;
import org.basex.core.Proc;
import org.basex.core.Prop;
import org.basex.core.Commands.Cmd;
import org.basex.io.BufferInput;
import org.basex.io.PrintOutput;
import org.basex.util.Token;

/**
 * This wrapper sends commands to the server instance over a socket
 * connection. It extends the {@link Session} class.
 *
 * The following steps show how to talk to the server instance with any
 * other programming language:
 * <ul>
 * <li> A socket instance is created by the constructor.</li>
 * <li> The {@link #execute} method sends database commands to the server.
 * All strings are encoded as UTF8 and concluded by a zero byte.</li>
 * <li> If the command was successfully processed,
 * the query string is sent.</li>
 * <li> Next, the processing info string is sent.</li>
 * <li> A last byte is next sent to indicate if command execution
 * was successful (0) or not (1).</li>
 * <li> {@link #close} closes the session by sending the {@link Cmd#EXIT}
 * command to the server.</li>
 * </ul>
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class ClientSession extends Session {
  /** Output stream. */
  private final PrintOutput out;
  /** Input stream. */
  private final InputStream in;
  /** Process info. */
  private String info;

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
    final Socket socket = new Socket(host, port);
    in = socket.getInputStream();
    out = new PrintOutput(socket.getOutputStream());

    // send user name and password
    out.print(user);
    out.write(0);
    out.print(Token.md5(pw));
    out.write(0);
    if(in.read() != 0) throw new LoginException();
  }

  @Override
  public boolean execute(final String cmd, final OutputStream o)
      throws IOException {

    out.print(cmd);
    out.write(0);
    final BufferInput bi = new BufferInput(in);
    int l;
    while((l = bi.read()) != 0) o.write(l);
    info = bi.readString();
    return bi.read() == 0;
  }

  @Override
  public boolean execute(final Proc pr, final OutputStream o)
      throws IOException {
    return execute(pr.toString(), o);
  }

  @Override
  public String info() {
    return info;
  }

  @Override
  public void close() throws IOException {
    execute(Cmd.EXIT.toString(), null);
  }
}
