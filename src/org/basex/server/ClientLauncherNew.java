package org.basex.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;

import org.basex.core.ALauncher;
import org.basex.core.Commands;
import org.basex.core.Process;
import org.basex.io.IO;
import org.basex.io.PrintOutput;

/**
 * This class sends client commands to the server instance over a socket.
 * It extends the {@link ALauncher} class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class ClientLauncherNew extends ALauncher {
  /** Temporary socket instance. */
  private Socket socket;
  /** Last socket reference. */
  private int last;

  /**
   * Constructor, specifying the server host:port and the command to be sent.
   * @param pr process
   * @param s socket reference
   */
  public ClientLauncherNew(final Process pr, final Socket s) {
    super(pr);
    socket = s;
  }

  @Override
  public boolean execute() {
    try {
      send(proc.toString());
      last = new DataInputStream(socket.getInputStream()).readInt();
    } catch(final IOException ex) {
      if(ex instanceof SocketException || ex instanceof EOFException) {
        return false;
      }
      ex.printStackTrace();
    }
    return last > 0;
  }

  @Override
  public void out(final PrintOutput o) throws IOException {
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
    final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    out.writeUTF(command);
    out.flush();
  }

  /**
   * Receives an input stream over the network.
   * @param o output stream
   * @throws IOException I/O Exception
   */
  private void receive(final PrintOutput o) throws IOException {
    final InputStream in = socket.getInputStream();
    final byte[] bb = new byte[IO.BLOCKSIZE];
    int l = 0;
    while((l = in.read(bb)) != -1) {
      for(int i = 0; i < l - 1; i++) o.write(bb[i]);
      if(l < IO.BLOCKSIZE) break;
    }
  }
}
