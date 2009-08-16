package org.basex.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
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
  /** Host name. */
  private final String host;
  /** Port number. */
  private final int port;
  /** Temporary socket instance. */
  private Socket socket;
  /** Last socket reference. */
  private int last;

  /**
   * Constructor, specifying the server host:port and the command to be sent.
   * @param ctx database context
   */
  public ClientLauncher(final Context ctx) {
    host = ctx.prop.get(Prop.HOST);
    port = ctx.prop.num(Prop.PORT);
  }

  @Override
  public boolean execute(final Process pr) throws IOException {
    send(pr.toString());
    last = new DataInputStream(socket.getInputStream()).readInt();
    socket.close();
    return last > 0;
  }

  @Override
  public void output(final PrintOutput o) throws IOException {
    send(Commands.Cmd.INTOUTPUT + " " + last);
    receive(o);
  }

  @Override
  public void info(final PrintOutput o) throws IOException {
    send(Commands.Cmd.INTINFO + " " + last);
    receive(o);
  }

  /**
   * Sends the specified string over the network.
   * @param command command to be sent
   * @throws IOException I/O exception
   */
  private void send(final String command) throws IOException {
    socket = new Socket(host, port);
    new DataOutputStream(socket.getOutputStream()).writeUTF(command);
  }

  /**
   * Receives an input stream over the network.
   * @param o output stream
   * @throws IOException I/O exception
   */
  private void receive(final PrintOutput o) throws IOException {
    final InputStream in = socket.getInputStream();
    final byte[] bb = new byte[IO.BLOCKSIZE];
    int l = 0;
    while((l = in.read(bb)) != -1) {
      for(int i = 0; i < l; i++) o.write(bb[i]);
    }
    socket.close();
  }
}
