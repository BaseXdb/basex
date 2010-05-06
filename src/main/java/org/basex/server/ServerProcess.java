package org.basex.server;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.net.Socket;
import org.basex.BaseXServer;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Proc;
import org.basex.core.Prop;
import org.basex.core.proc.Close;
import org.basex.core.proc.Exit;
import org.basex.data.Data;
import org.basex.data.XMLSerializer;
import org.basex.io.BufferInput;
import org.basex.io.BufferedOutput;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.util.Performance;

/**
 * Single session for a client-server connection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public final class ServerProcess extends Thread {
  /** Database context. */
  public final Context context;
  /** Socket reference. */
  private final Socket socket;
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
    log = b.log;
    socket = s;
  }

  /**
   * Initializes the session via cram-md5 authentication.
   * @return success flag
   */
  public boolean init() {
    try {
      final String ts = Long.toString(System.nanoTime());

      // send timestamp (cram-md5)
      out = new PrintOutput(new BufferedOutput(socket.getOutputStream()));
      out.print(ts);
      send(true);

      // evaluate login data
      in = new BufferInput(socket.getInputStream());
      final String us = in.readString();
      final String pw = in.readString();
      context.user = context.users.get(us);
      final boolean ok = context.user != null &&
        md5(string(context.user.pw) + ts).equals(pw);
      send(ok);

      if(ok) start();
      else if(!us.isEmpty()) log.write(this, "LOGIN " + us, "failed");
      return ok;
    } catch(final IOException ex) {
      ex.printStackTrace();
      log.write(ex.getMessage());
      return false;
    }
  }

  @Override
  public void run() {
    log.write(this, "LOGIN " + context.user.name, "OK");
    String input = null;
    try {
      while(true) {
        try {
          byte b = in.readByte();
          if(b == 0) {
            iterate();
            return;
          }
          input = in.readString(b);
        } catch(final IOException ex) {
          // this exception is thrown for each session if the server is stopped
          exit();
          break;
        }

        // parse input and create process instance
        final Performance perf = new Performance();
        proc = null;
        try {
          final Proc[] procs = new CommandParser(input, context, true).parse();
          if(procs.length != 1)
            throw new QueryException(SERVERPROC, procs.length);

          proc = procs[0];
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
        final boolean ok = proc.exec(context, out);
        out.write(0);
        final String inf = proc.info();
        out.print(inf.equals(PROGERR) ? SERVERTIME : inf);
        out.write(0);
        send(ok);
        stopTimer();
        final String pr = proc.toString().replaceAll("\\r|\\n", " ");
        log.write(this, pr, ok ? "OK" : INFOERROR + inf, perf);
      }
      log.write(this, "LOGOUT " + context.user.name, "OK");
    } catch(final IOException ex) {
      log.write(this, input, INFOERROR + ex.getMessage());
      ex.printStackTrace();
      exit();
    }
  }
  
  
  /**
   * Query is executed in iterate mode.
   * @throws IOException Exception
   */
  private void iterate() throws IOException {
    String input = in.readString();
    QueryProcessor processor = new QueryProcessor(input, context);
    try {
      Iter iter = processor.iter();
      XMLSerializer serializer = new XMLSerializer(out);
      Item item;
      while((item = iter.next()) != null) {
          item.serialize(serializer);
      }
      out.write(0);
      out.print("DONE");
      out.write(0);
      send(true);
      serializer.close();
      processor.close();
    } catch(QueryException ex) {
   // invalid command was sent by a client; create error feedback
      log.write(this, input, INFOERROR + ex.extended());
      out.write(0);
      out.print(ex.extended());
      out.write(0);
      send(false);
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
    new Close().exec(context);
    if(proc != null) proc.stop();
    stopTimer();
    context.delete(this);

    try {
      socket.close();
    } catch(final IOException ex) {
      log.write(ex.getMessage());
      ex.printStackTrace();
    }
  }

  /**
   * Returns session information.
   * @return database information
   */
  String info() {
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
