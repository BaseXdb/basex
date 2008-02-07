package org.basex.core;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import org.basex.io.PrintOutput;

/**
 * This class sends client commands to the server instance over a socket.
 * It extends the {@link AbstractProcess} class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class ClientProcess extends AbstractProcess {
  /** Host name. */
  private final String host;
  /** Port number. */
  private final int port;
  /** Temporary socket instance. */
  private Socket socket;

  /**
   * Constructor, specifying the server host:port and the command to be sent.
   * @param h name of the host
   * @param p port
   * @param c command
   */
  public ClientProcess(final String h, final int p, final Command c) {
    host = h;
    port = p;
    cmd = c;
  }
  
  @Override
  public boolean execute() throws IOException {
    send(cmd.name + " " + cmd.args());
    return socket.getInputStream().read() != 0;
  }

  @Override
  public void output(final PrintOutput o) throws IOException {
    send(Commands.GETRESULT.name());
    receive(o);
  }

  @Override
  public void info(final PrintOutput o) throws IOException {
    send(Commands.GETINFO.name());
    receive(o);
  }

  /**
   * Sends the specified command and argument over the network.
   * @param command command to be sent
   * @throws IOException I/O Exception
   */
  private void send(final String command) throws IOException {
    socket = new Socket(host, port);
    new DataOutputStream(socket.getOutputStream()).writeUTF(command);
  }

  /**
   * Receive input stream over the network.
   * @param o output stream
   * @throws IOException I/O Exception
   */
  private void receive(final PrintOutput o) throws IOException {
    final InputStream in = socket.getInputStream();
    final byte[] bb = new byte[4096];
    int l = 0;
    while((l = in.read(bb)) != -1) for(int i = 0; i < l; i++) o.write(bb[i]);
  }
}
