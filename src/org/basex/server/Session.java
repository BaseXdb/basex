package org.basex.server;

import static org.basex.Text.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

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
import org.basex.util.Performance;

/**
 * Session for a Client Server Connection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class Session implements Runnable {
  
  /** Database Context. */
  final Context context = new Context();
  /** Socket. */
  Socket socket;
  /** ClientId. */
  int clientId;
  /** Verbose mode. */
  boolean verbose;
  /** Core. */
  Process core;
  /** Timeout Thread. */
  Thread timeout;
  /** Session Thread. */
  Thread session = null;

  /** Flag for Session. */
  boolean running = true;
  /** DataOutputStream. */
  DataOutputStream dos;
  /** DataInputStream. */
  DataInputStream dis;
  /** PrintOutput. */
  PrintOutput out;
  /** BaseXServerNew. */
  BaseXServerNew bx;
  
  
  /**
   * Session.
   * @param s Socket
   * @param c ClientId
   * @param v Verbose Mode
   * @param b BaseXServerNew
   */
  public Session(final Socket s, final int c, final boolean v,
      final BaseXServerNew b) {
    this.clientId = c;
    this.socket = s;
    this.verbose = v;
    this.bx = b;
  }
  
  /**
   * Starts the Thread.
   */
  public synchronized void start() {
    if (session == null) {
      session = new Thread(this);
      session.start();
    }
  }
  
  /**
   * Handles Client Server Communication.
   * @throws IOException I/O Exception
   */
  private void handle() throws IOException {
    BaseX.outln("Login from Client " + clientId);
    final Performance perf = new Performance();
    final InetAddress addr = socket.getInetAddress();
    final String ha = addr.getHostAddress();
    final int sp = socket.getPort();
    // get command and arguments
    dis = new DataInputStream(socket.getInputStream());
    dos = new DataOutputStream(socket.getOutputStream());
    out = new PrintOutput(new BufferedOutput(socket.getOutputStream()));
    final int port = socket.getPort();
    String in;
    while (running) {
      in = getMessage().trim(); 
      if(verbose) BaseX.outln("[%:%] %", ha, port, in);
      Process pr = null;
      try {
        pr = new CommandParser(in).parse()[0];
      } catch(final QueryException ex) {
        pr = new Process(0) { };
        pr.error(ex.extended());
        core = pr;
        send(-sp);
        return;
      }
      if(pr instanceof Exit) {
        send(0);
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
          out.write(0);
        } else if(proc instanceof GetInfo) {
          // the client requests information about the last process
          c.info(out);
          out.write(0);
        }
        out.flush();
      } else {
        core = proc;
        timeout(proc);
        send(proc.execute(context) ? sp : -sp);
        timeout.interrupt();
      }
      if(verbose) BaseX.outln("[%:%] %", ha, sp, perf.getTimer());
    }
    stop();
  }
  
  /**
   * Times out a process.
   * @param proc process reference
   */
  private void timeout(final Process proc) {
    timeout = new Thread() {
      @Override
      public void run() {
        Performance.sleep(5000);  //Prop.timeout * 1000
        proc.stop();
      }
    };
    timeout.start();
  }
  
  /**
   * Returns the Message from the Client.
   * @return String
   * @throws IOException I/O Exception
   */
  synchronized String getMessage() throws IOException {
    return dis.readUTF();
  }
  
  /**
   * Returns an answer to the client.
   * @param id session id to be returned
   * @throws IOException I/O exception
   */
  synchronized void send(final int id) throws IOException {
    dos.writeInt(id);
    dos.flush();
  }
  
  /**
   * Stops the thread. 
   */
  synchronized void stop() {
    BaseX.outln("Client " + clientId + " has logged out.");
    bx.sessions.remove(this);
    try {
      socket.close();
    } catch(IOException e) {
      e.printStackTrace();
    }
    session = null;
  }
  
  public void run() {
    try {
      handle(); 
    } catch(Exception io) {
      // for forced stops
      if (io instanceof SocketException) {
       stop();
      } else {
      io.printStackTrace();
      }
    }
  }
}
