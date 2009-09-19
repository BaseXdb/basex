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
 * This wrapper sends client commands to the server instance over a socket.
 * It extends the {@link Session} class.
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
   * Constructor, specifying the server host:port and the command to be sent.
   * @param context database context
   * @throws IOException I/O exception
   */
  public ClientSession(final Context context) throws IOException {
    this(context.prop.get(Prop.HOST), context.prop.num(Prop.PORT));
  }

  /**
   * Constructor, specifying the server host:port and the command to be sent.
   * @param name server name
   * @param port server port
   * @throws IOException I/O exception
   */
  public ClientSession(final String name, final int port) throws IOException {
    final Socket socket = new Socket(name, port);
    in = socket.getInputStream();
    out = new DataOutputStream(socket.getOutputStream());
  }

  @Override
  public boolean execute(final String cmd) throws IOException {
    out.writeUTF(cmd);
    return in.read() == 0;
  }

  @Override
  public boolean execute(final Process pr) throws IOException {
    return execute(pr.toString());
  }

  @Override
  public void output(final PrintOutput o) throws IOException {
    out.writeUTF(Cmd.INTOUTPUT.toString());
    final BufferInput bi = new BufferInput(in);
    int l;
    while((l = bi.read()) != 0) o.write(l);
    for(int i = 0; i < IO.BLOCKSIZE - 1; i++) bi.read();
  }

  @Override
  public void info(final PrintOutput o) throws IOException {
    out.writeUTF(Cmd.INTINFO.toString());
    o.print(new DataInputStream(in).readUTF());
  }

  @Override
  public void close() throws IOException {
    execute(Cmd.EXIT.toString());
  }
}
