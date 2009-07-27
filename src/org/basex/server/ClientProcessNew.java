package org.basex.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import org.basex.core.AbstractProcess;
import org.basex.core.Commands;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.io.IO;
import org.basex.io.PrintOutput;

/**
 * This class sends client commands to the server instance over a socket. It
 * extends the {@link AbstractProcess} class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class ClientProcessNew extends AbstractProcess {
  /** Process reference. */
  private final Process proc;
  /** Socket instance. */
  private final Socket socket;
  /** Last socket reference. */
  private int last;

  /**
   * Constructor, specifying the server host:port and the command to be sent.
   * @param s Socket
   * @param pr process
   */
  public ClientProcessNew(final Socket s, final Process pr) {
    socket = s;
    proc = pr;
  }

  @Override
  public boolean execute(final Context ctx) {
    try {
      send(proc.toString());
      last = new DataInputStream(socket.getInputStream()).readInt();
    } catch(final IOException e) {
      if(e instanceof SocketException || e instanceof EOFException) {
        return false;
      }
      e.printStackTrace();
    }
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
  synchronized void send(final String command) throws IOException {
    final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    out.writeUTF(command);
    out.flush();
  }

  /**
   * Receives an input stream over the network.
   * @param o output stream
   * @throws IOException I/O Exception
   */
  synchronized void receive(final PrintOutput o) throws IOException {
    final InputStream in = socket.getInputStream();
    final byte[] bb = new byte[IO.BLOCKSIZE];
    int l = 0;
    while((l = in.read(bb)) != -1) {
      for(int i = 0; i < l - 1; i++) o.write(bb[i]);
      if(l < IO.BLOCKSIZE) break;
    }
  }
}
