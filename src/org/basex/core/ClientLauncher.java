package org.basex.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import org.basex.core.proc.IntInfo;
import org.basex.core.proc.IntOutput;
import org.basex.io.BufferInput;
import org.basex.io.IO;
import org.basex.io.PrintOutput;

/**
 * This class sends client commands to the server instance over a socket.
 * It extends the {@link ALauncher} class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class ClientLauncher extends ALauncher {
  /** Output stream. */
  private final DataOutputStream out;
  /** Input stream. */
  private final InputStream in;

  /**
   * Constructor, specifying the server host:port and the command to be sent.
   * @param ctx database context
   * @throws IOException I/O exception
   */
  public ClientLauncher(final Context ctx) throws IOException {
    final Socket socket = new Socket(
        ctx.prop.get(Prop.HOST), ctx.prop.num(Prop.PORT));
    in = socket.getInputStream();
    out = new DataOutputStream(socket.getOutputStream());
  }

  @Override
  public boolean execute(final Process pr) throws IOException {
    send(pr);
    return in.read() == 0;
  }

  @Override
  public void output(final PrintOutput o) throws IOException {
    send(new IntOutput(""));
    final BufferInput bi = new BufferInput(in);
    int l;
    while((l = bi.read()) != 0) o.write(l);
    for(int i = 0; i < IO.BLOCKSIZE - 1; i++) bi.read();
  }

  @Override
  public void info(final PrintOutput o) throws IOException {
    send(new IntInfo(""));
    o.print(new DataInputStream(in).readUTF());
  }

  /**
   * Sends the specified process.
   * @param pr process to send
   * @throws IOException I/O exception
   */
  private void send(final Process pr) throws IOException {
    out.writeUTF(pr.toString());
  }
}
