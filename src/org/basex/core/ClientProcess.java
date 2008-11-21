package org.basex.core;

import java.io.DataInputStream;
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
  /** Process reference. */
  private Process proc;
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
   * @param h name of the host
   * @param p port
   * @param pr process
   */
  public ClientProcess(final String h, final int p, final Process pr) {
    host = h;
    port = p;
    proc = pr;
  }
  
  @Override
  public boolean execute(final Context ctx) throws IOException {
    send(proc.toString());
    last = new DataInputStream(socket.getInputStream()).readInt();
    return last > 0;
  }

  @Override
  public void output(final PrintOutput o) throws IOException {
    send(Commands.Cmd.GETRESULT.name() + " " + last);
    receive(o);
  }

  @Override
  public void info(final PrintOutput o) throws IOException {
    send(Commands.Cmd.GETINFO.name() + " " + last);
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
