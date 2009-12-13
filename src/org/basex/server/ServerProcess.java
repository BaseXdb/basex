package org.basex.server;

import static org.basex.core.Text.*;
import java.io.IOException;
import java.net.Socket;
import org.basex.BaseXServer;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Proc;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.core.proc.Close;
import org.basex.core.proc.Exit;
import org.basex.data.Data;
import org.basex.io.BufferInput;
import org.basex.io.BufferedOutput;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.util.Performance;

/**
 * Single session for a client-server connection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class ServerProcess extends Thread {
  /** Database context. */
  public final Context context;
  /** Socket reference. */
  private final Socket socket;
  /** Server reference. */
  private final Semaphore sem;
  /** Input stream. */
  private BufferInput in;
  /** Output stream. */
  private PrintOutput out;
  /** Current process. */
  private Proc proc;
  /** Timeout thread. */
  private Thread timeout;
  /** Log. */
  private final Log log;

  /**
   * Constructor.
   * @param s socket
   * @param b server reference
   */
  public ServerProcess(final Socket s, final BaseXServer b) {
    context = new Context(b.context);
    sem = b.sem;
    log = b.log;
    socket = s;
  }

  /**
   * Initializes the session.
   * @return success flag
   */
  public boolean init() {
    try {
      in = new BufferInput(socket.getInputStream());
      out = new PrintOutput(new BufferedOutput(socket.getOutputStream()));

      // evaluate login data
      final String us = in.readString();
      final String pw = in.readString();
      context.user = context.users.get(us, pw);
      final boolean ok = context.user != null;
      send(ok);

      if(ok) start();
      else if(us.length() != 0) log.write(this, "LOGIN " + us, "failed");
      return ok;
    } catch(final IOException ex) {
      Main.error(ex, false);
      log.write(ex.getMessage());
      return false;
    }
  }

  @Override
  public void run() {
    log.write(this, "LOGIN " + context.user.name, "successful");
    String input = null;
    try {
      while(true) {
        try {
          input = in.readString();
        } catch(final IOException ex) {
          // this exception is thrown for each session if the server is stopped
          exit();
          break;
        }

        // parse input and create process instance
        final Performance perf = new Performance();
        proc = null;
        try {
          proc = new CommandParser(input, context, true).parse()[0];
        } catch(final QueryException ex) {
          // invalid command was sent by a client; create error feedback
          log.write(this, input, perf, INFOERROR + ex.extended());
          out.write(0);
          out.print(ex.extended());
          out.write(0);
          send(false);
          continue;
        }

        // stop console
        if(proc instanceof Exit) {
          exit();
          break;
        }

        // process command and send results
        startTimer(proc);
        final boolean up = proc.updating(context) ||
          (proc.flags & User.CREATE) != 0;
        sem.before(up);
        final boolean ok = proc.execute(context, out);
        out.write(0);
        final String inf = proc.info();
        out.print(inf.equals(PROGERR) ? SERVERTIME : inf);
        out.write(0);
        send(ok);
        stopTimer();
        sem.after(up);
        log.write(this, proc, perf, ok ? "OK" : INFOERROR + inf);
      }
      log.write(this, "LOGOUT " + context.user.name);
    } catch(final IOException ex) {
      log.write(this, input, INFOERROR + ex.getMessage());
      Main.error(ex, false);
    }
  }

  /**
   * Sends the success flag to the client.
   * @param ok success flag
   * @throws IOException I/O exception
   */
  private void send(final boolean ok) throws IOException {
    out.write(ok ? 0 : 1);
    out.flush();
  }

  /**
   * Starts a timeout thread for the specified process.
   * @param p process reference
   */
  private void startTimer(final Proc p) {
    final long to = context.prop.num(Prop.TIMEOUT);
    if(to == 0) return;

    timeout = new Thread() {
      @Override
      public void run() {
        Performance.sleep(to * 1000);
        System.out.println("?");
        p.stop();
      }
    };
    timeout.start();
  }

  /**
   * Stops the current timeout thread.
   */
  private void stopTimer() {
    if(timeout != null) timeout.interrupt();
  }

  /**
   * Exits the session.
   */
  public void exit() {
    new Close().execute(context);
    if(proc != null) proc.stop();
    stopTimer();
    context.delete(this);

    try {
      socket.close();
    } catch(final IOException ex) {
      log.write(ex.getMessage());
      Main.error(ex, false);
    }
  }

  /**
   * Returns session information.
   * @return database information
   */
  public String info() {
    final Data data = context.data;
    return this + (data != null ? ": " + data.meta.name : "");
  }

  @Override
  public String toString() {
    final String host = socket.getInetAddress().getHostAddress();
    final int port = socket.getPort();
    return Main.info("[%:%]", host, port);
  }
}
