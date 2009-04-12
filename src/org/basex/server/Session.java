package org.basex.server;

import static org.basex.Text.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.basex.BaseX;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.Exit;
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
  /** Verbose mode. */
  boolean verbose = false;
  /** Core. */
  Process core;
  /** Flag for Session. */
  boolean running = true;
  
  
  /**
   * Session.
   * @param s Socket
   * @param c ClientId
   */
  public Session(final Socket s, final int c) {
    super("Session");
    this.clientId = c;
    this.socket = s;
  }
  
  /**
   * Handles Client Server Communication.
   * @throws IOException I/O Exception
   */
  private void handle() throws IOException {
    //final Performance perf = new Performance();
    // get command and arguments
    DataInputStream dis = new DataInputStream(socket.getInputStream());
    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
    PrintOutput out = new PrintOutput(new BufferedOutput(
        socket.getOutputStream()));
    final int sp = socket.getPort();
    String in;
    while (running) {
      in = getMessage(dis).trim(); 
      Process pr = null;
      try {
        pr = new CommandParser(in).parse()[0];
      } catch(final QueryException ex) {
        pr = new Process(0) { };
        pr.error(ex.extended());
        core = pr;
        send(-sp, dos);
        return;
      }
      if(pr instanceof Exit) {
        send(0, dos);
        BaseX.outln("Client " + clientId + " has logged out.");
        // interrupt running processes
        running = false;
        break;
      }
      Process proc = pr;
      if(proc instanceof GetResult || proc instanceof GetInfo) {
        Process c = core;
        if(c == null) {
          out.print(BaseX.info(SERVERTIME, Prop.timeout));
        } else if(proc instanceof GetResult) {
          // the client requests result of the last process
          c.output(out);
        } else if(proc instanceof GetInfo) {
          // the client requests information about the last process
          c.info(out);
          out.write(0);
        }
        out.flush();
      } else {
        core = proc;
        send(proc.execute(context) ? sp : -sp, dos);
      }
    }
    //out.close();
    //dos.close();
    //dis.close();
    //socket.close();
    this.interrupt();
  }
  
  /**
   * Returns the Message from the Client.
   * @param dis DataInputStream
   * @return String
   * @throws IOException I/O Exception
   */
  synchronized String getMessage(final DataInputStream dis) throws IOException {
    return dis.readUTF();
  }
  
  /**
   * Returns an answer to the client.
   * @param id session id to be returned
   * @param dos DataOutputStream
   * @throws IOException I/O exception
   */
  synchronized void send(final int id, final DataOutputStream dos)
  throws IOException {
    dos.writeInt(id);
    dos.flush();
    }
  
  @Override
  public void run() {
    try {
      handle(); 
    } catch(IOException io) {
      io.printStackTrace();
    }
  }
}
