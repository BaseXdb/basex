package org.basex.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import org.basex.core.Session;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.Commands.Cmd;
import org.basex.io.BufferInput;
import org.basex.io.IO;
import org.basex.io.PrintOutput;

/**
 * This wrapper sends commands to the server instance over a socket
 * connection. It extends the {@link Session} class.
 *
 * The following steps show how to talk to the server instance with any
 * other programming language:
 * <ul>
 * <li> A socket is created by the constructor.</li>
 * <li> The {@link #execute} method sends database commands to the server
 * as UTF8 byte arrays. The byte array is preceded by two bytes (high/low byte)
 * containing the string length. A single byte is received as result,
 * determining if command execution was successful (0) or not (1).
 * If the command was successful, the query result is sent.
 * As the length of the resulting byte array is unknown,
 * it is concluded by {@link IO#BLOCKSIZE} zero bytes.</li>
 * <li> The {@link #info} method sends the {@link Cmd#INTINFO} command and
 * receives a UTF8 byte array as result, which has the format as the one sent
 * by {@link #execute}.</li>
 * <li> {@link #close} closes the session by sending the {@link Cmd#EXIT}
 * command to the server.</li>
 * </ul>
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class ClientSession extends Session {
  /** Output stream. */
  private final DataOutputStream out;
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
    final Socket socket = new Socket(host, port);
    in = socket.getInputStream();
    out = new DataOutputStream(socket.getOutputStream());

    // send user name and password
    out.writeUTF(user);
    out.writeUTF(pw);
    if(in.read() != 0) throw new LoginException();
  }

  @Override
  public boolean execute(final String cmd, final PrintOutput o)
      throws IOException {

    out.writeUTF(cmd);
    final BufferInput bi = new BufferInput(in);
    int l;
    while((l = bi.read()) != 0) o.write(l);
    for(int i = 0; i < IO.BLOCKSIZE - 1; i++) bi.read();
    return bi.read() == 0;
  }

  @Override
  public boolean execute(final Process pr, final PrintOutput o)
      throws IOException {
    return execute(pr.toString(), o);
  }

  @Override
  public String info() throws IOException {
    out.writeUTF(Cmd.INTINFO.toString());
    return new DataInputStream(in).readUTF();
  }

  @Override
  public void close() throws IOException {
    execute(Cmd.EXIT.toString(), null);
  }
}
