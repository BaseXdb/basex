package org.basex.server;

import static org.basex.Text.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.basex.BaseX;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.GetInfo;
import org.basex.core.proc.GetResult;
import org.basex.io.BufferedOutput;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;

/**
 * Session for a Client Server Connection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class Session extends Thread {
  
  /** Database Context. */
  final Context context = new Context();
  /** Socket. */
  private Socket socket;
  /** ClientId. */
  private int clientId;
  /** Process. */
  Process core;
  /** Verbose mode. */
  boolean verbose;
  
  /**
   * Session.
   * @param s Socket
   * @param c ClientId
   */
  public Session(final Socket s, final int c) {
    super("Session");
    socket = s;
    clientId = c;
    System.out.println("Connection on " + socket + " from " + clientId);
  }
  
  /**
   * Handles Client Server Communication.
   * @throws IOException I/O Exception
   */
  private void handle() throws IOException {
 // get command and arguments
    final DataInputStream dis = new DataInputStream(socket.getInputStream());
    final String in = dis.readUTF().trim();
    final InetAddress addr = socket.getInetAddress();
    final String ha = addr.getHostAddress();
    final int sp = socket.getPort();
    if(verbose) BaseX.outln("[%:%] %", ha, sp, in);
    
    Process pr = null;
    try {
      pr = new CommandParser(in).parse()[0];
    } catch(final QueryException ex) {
      pr = new Process(0) { };
      pr.error(ex.extended());
      core = pr;
      send(socket, -sp);
      return;
    }
    final Process proc = pr;
    if(proc instanceof GetResult || proc instanceof GetInfo) {
      final OutputStream os = socket.getOutputStream();
      final PrintOutput out = new PrintOutput(new BufferedOutput(os));
      //final int id = Math.abs(Integer.parseInt(proc.args().trim()));
      final Process c = core;
      if(c == null) {
        out.print(BaseX.info(SERVERTIME, Prop.timeout));
      } else if(proc instanceof GetResult) {
        // the client requests result of the last process
        c.output(out);
      } else if(proc instanceof GetInfo) {
        // the client requests information about the last process
        c.info(out);
      }
      out.close();
    } else {
      // process a normal request
      core = proc;
      //add(new BaseXSession(sp, System.nanoTime(), proc));
      // execute command and return process id (negative: error)
      send(socket, proc.execute(context) ? sp : -sp);
      //send(s, proc.execute(context) ? sp : -sp);
    }
    dis.close();
  }
  
  /**
   * Returns an answer to the client.
   * @param s socket reference
   * @param id session id to be returned
   * @throws IOException I/O exception
   */
  synchronized void send(final Socket s, final int id) throws IOException {
    final DataOutputStream dos = new DataOutputStream(s.getOutputStream());
    dos.writeInt(id);
    dos.close();
  }
  
  @Override
  public void run() {
    try {
      handle();
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

}
