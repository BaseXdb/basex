package org.basex.server;

import static org.basex.Text.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import org.basex.BaseX;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.Process;
import org.basex.core.proc.Close;
import org.basex.core.proc.Exit;
import org.basex.core.proc.GetInfo;
import org.basex.core.proc.GetResult;
import org.basex.io.BufferedOutput;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.util.Performance;

/**
 * Session for a client-server connection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
final class Session implements Runnable {
  /** Database context. */
  final Context context = new Context();
  /** Socket. */
  final Socket socket;
  /** Client id. */
  final int clientId;
  /** Verbose mode. */
  final boolean info;
  /** Core. */
  Process core;
  /** Timeout thread. */
  Thread timeout;
  /** Session thread. */
  Thread session = null;

  /** Flag for session. */
  boolean running = true;
  /** Output stream. */
  DataOutputStream dos;
  /** Input stream. */
  DataInputStream dis;
  /** Print output. */
  PrintOutput out;
  /** Server reference. */
  BaseXServerNew bxs;

  /**
   * Session.
   * @param s socket
   * @param c client id
   * @param i verbose Mode
   * @param b server reference
   */
  Session(final Socket s, final int c, final boolean i,
      final BaseXServerNew b) {
    clientId = c;
    socket = s;
    info = i;
    bxs = b;
  }

  /**
   * Starts the thread.
   */
  void start() {
    if(session == null) {
      session = new Thread(this);
      session.start();
    }
  }

  /**
   * Handles client-server communication.
   * @throws IOException I/O exception
   */
  private void handle() throws IOException {
    if(info) BaseX.outln("Login from Client %.", clientId);
    final Performance perf = new Performance();
    final InetAddress addr = socket.getInetAddress();
    final String ha = addr.getHostAddress();
    final int sp = socket.getPort();

    // get command and arguments
    dis = new DataInputStream(socket.getInputStream());
    dos = new DataOutputStream(socket.getOutputStream());
    out = new PrintOutput(new BufferedOutput(socket.getOutputStream()));
    final int port = socket.getPort();

    while(running) {
      final String in = getMessage().trim();
      if(in.equals("end")) {
        stop(false);
        break;
      }
      if(info) BaseX.outln("[%:%] %", ha, port, in);
      Process pr = null;
      try {
        pr = new CommandParser(in, context).parse()[0];
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
        stop(true);
        break;
      }
      final Process proc = pr;
      if(proc instanceof GetResult || proc instanceof GetInfo) {
        final Process c = core;
        if(c == null) {
          out.print(BaseX.info(SERVERTIME));
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
      if(info) BaseX.outln("[%:%] %", ha, sp, perf.getTimer());
    }
  }

  /**
   * Times out a process.
   * @param proc process reference
   */
  private void timeout(final Process proc) {
    timeout = new Thread() {
      @Override
      public void run() {
        Performance.sleep(context.prop.num(Prop.TIMEOUT) * 1000);
        proc.stop();
      }
    };
    timeout.start();
  }

  /**
   * Returns the message from the client.
   * @return message
   */
  synchronized String getMessage() {
    try {
      return dis.readUTF();
    } catch(final IOException ex) {
      ex.printStackTrace();
      return "end";
    }
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
   * Stops the session.
   * @throws IOException I/O exception
   */
  public void close() throws IOException {
    dis.close();
  }

  /**
   * Closes the session.
   * @param s boolean
   */
  public void stop(final boolean s) {
    running = false;
    if(s) bxs.sessions.remove(this);
    new Close().execute(context);
    if(info) BaseX.outln("Client % has logged out.", clientId);
    timeout = null;
    session = null;
    try {
      socket.close();
    } catch(final IOException ex) {
      ex.printStackTrace();
    }
  }

  public void run() {
    try {
      handle();
    } catch(final IOException ex) {
      ex.printStackTrace();
    }
  }
}
