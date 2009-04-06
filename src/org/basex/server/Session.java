package org.basex.server;

import static org.basex.Text.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
  /** Core. */
  Process core;
  /** Flag which handle to use. */
  boolean handle = true;
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
    PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
    BufferedReader is = new BufferedReader(new InputStreamReader(
        socket.getInputStream()));

    String in, out;
    out = "You are logged in to the BaseXServer";
    pw.println(out);

    while ((in = is.readLine()) != null) {
      if(in.equals("exit")) {
        System.out.println("Client " + clientId + " has logged out.");
        break;
      }
      pw.println("Echo from Server: " + in);
    }
    is.close();
    pw.close();
    socket.close();
  }
  
  /**
   * Handles Client Server Communication.
   * @throws IOException I/O Exception
   */
  private void handle2() throws IOException {
    //final Performance perf = new Performance();
    // get command and arguments
    DataInputStream dis = new DataInputStream(socket.getInputStream());
    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
    PrintOutput out = new PrintOutput(new BufferedOutput(
        socket.getOutputStream()));
    final int sp = socket.getPort();
    String in;
    //while ((in = dis.readUTF()) != null) { 
    while ((in = getMessage(dis).trim()) != null) {
      //in = getMessage(dis).trim();
      if(in.equals("exit")) {
        BaseX.outln("Client " + clientId + " has logged out.");
        break;
      }
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
        }
        out.flush();
      } else {
        core = proc;
        send(proc.execute(context) ? sp : -sp, dos);
      }
    }
    dis.close();
    socket.close();
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
      if(handle) handle2();
      handle();
    } catch(IOException e) {
      e.printStackTrace();
    }
  }
}
