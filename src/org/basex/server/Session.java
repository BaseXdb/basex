package org.basex.server;

import static org.basex.Text.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
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
  /** Verbose mode. */
  boolean verbose = false;
  /** Flag for server activity. */
  boolean running = true;
  /** Core. */
  Process core;
  /** Flag which handle to use. */
  boolean handle = true;
  
  /**
   * Session.
   * @param s Socket
   * @param c ClientId
   */
  public Session(final Socket s, final int c) {
    super("Session");
    this.socket = s;
    this.clientId = c;
  }
  
  /**
   * Handles Client Server Communication.
   * @throws IOException I/O Exception
   */
  private void handle() throws IOException {
    System.out.println("Login from Client " + clientId);
    PrintWriter os = new PrintWriter(socket.getOutputStream(), true);
    BufferedReader is = new BufferedReader(new InputStreamReader(
        socket.getInputStream()));

    String in, out;
    out = "You are logged in to the BaseXServer";
    os.println(out);

    while ((in = is.readLine()) != null) {
      if(in.equals("exit")) {
        System.out.println("Client " + clientId + " has logged out.");
        break;
      }
      os.println("Echo from Server: " + in);
    }
    is.close();
    os.close();
    socket.close();
  }
  
  /**
   * Handles Client Server Communication.
   * @throws IOException I/O Exception
   */
  private void handle2() throws IOException {
    System.out.println("Login from Client " + clientId);
    //final Performance perf = new Performance();
    // get command and arguments
    final DataInputStream dis = new DataInputStream(socket.getInputStream());
    final DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
    final int sp = socket.getPort();
    String in;
    while (running) { 
      in = dis.readUTF().trim();
      if(in.equals("exit")) {
        System.out.println("Client " + clientId + " has logged out.");
        running = false;
      }
      Process pr = null;
      try {
        pr = new CommandParser(in).parse()[0];
      } catch(final QueryException ex) {
        pr = new Process(0) { };
        pr.error(ex.extended());
        core = pr;
        dos.writeInt(-sp);
      }
      final Process proc = pr;
      if(proc instanceof GetResult || proc instanceof GetInfo) {
        final OutputStream os = socket.getOutputStream();
        final PrintOutput out = new PrintOutput(new BufferedOutput(os));
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
        core = proc;
        dos.writeInt(proc.execute(context) ? sp : -sp);
      }
    }
    dis.close();
    dos.close();
    socket.close();
  }
  
  @Override
  public void run() {
    try {
      if(handle) handle2();
      handle();
    } catch(IOException e) {
      e.printStackTrace();
    }
  }
}
