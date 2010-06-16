package org.basex.server;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import org.basex.BaseXServer;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Proc;
import org.basex.core.Prop;
import org.basex.core.proc.Close;
import org.basex.core.proc.Exit;
import org.basex.data.Data;
import org.basex.io.BufferInput;
import org.basex.io.BufferedOutput;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

/**
 * Single session for a client-server connection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
public final class ServerProcess extends Thread {
  /** Database context. */
  public final Context context;
  /** Socket reference. */
  private final Socket socket;
  /** Log reference. */
  private final Log log;

  /** Input stream. */
  private BufferInput in;
  /** Output stream. */
  private PrintOutput out;
  /** Current process. */
  private Proc proc;
  /** Query id counter. */
  private int id;

  /** Active queries. */
  final HashMap<String, QueryProcess> queries =
    new HashMap<String, QueryProcess>();

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

      // send {TIMESTAMP}0
      out = new PrintOutput(new BufferedOutput(socket.getOutputStream()));
      out.print(ts);
      send(true);

      // evaluate login data
      in = new BufferInput(socket.getInputStream());
      // reveive {USER}\0{PASSWORD}\0
      final String us = in.readString();
      final String pw = in.readString();
      context.user = context.users.get(us);
      final boolean ok = context.user != null &&
        md5(string(context.user.pw) + ts).equals(pw);
      // send {OK}
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
          // receive first byte
          final byte b = in.readByte();
          if(b < ' ') {
            // handle control codes
            query(b);
            continue;
          }
          // receive complete command
          input = new TokenBuilder().add(b).add(in.content()).toString();
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
          // log invalid command
          final String msg = ex.extended();
          log.write(this, input, INFOERROR + msg);
          // send 0 to mark end of potential result
          out.write(0);
          // send {INFO}0
          out.writeString(msg);
          // send 1 to mark error
          send(false);
          continue;
        }

        // stop console
        if(proc instanceof Exit) {
          exit();
          break;
        }

        // start timeout
        proc.startTimeout(context.prop.num(Prop.TIMEOUT));

        // process command and send {RESULT}0
        final boolean ok = proc.exec(context, out);
        final String inf = proc.info();
        out.write(0);
        // send {INFO}0
        out.writeString(inf.equals(PROGERR) ? SERVERTIMEOUT : inf);
        // send {OK}
        send(ok);

        // stop timeout
        proc.stopTimeout();

        final String pr = proc.toString().replace('\r', ' ').replace('\n', ' ');
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
   * Process the query iterator.
   * @param c control code (first received byte from client)
   * @throws IOException I/O exception
   */
  private void query(final byte c) throws IOException {
    // iterator argument
    String arg = in.readString();

    QueryProcess qp = null;
    if(c == 0) {
      // c = 0: create new query process
      qp = new QueryProcess(arg, out, this);
      arg = Integer.toString(id++);
      queries.put(arg, qp);
    } else {
      // find query process
      qp = queries.get(arg);
    }

    boolean close = false;
    try {
      if(c == 0) {
        // c = 0: initialize iterator
        if(qp != null) qp.init();
        // send {ID}0 and 0 as success flag
        out.writeString(arg);
        out.write(0);
      } else if(c == 1) {
        // c = 1: request next item
        if(qp != null) close = qp.next();
        // send 0 to mark end of result and 0 as success flag
        out.write(0);
        out.write(0);
      } else if(c == 2) {
        // c = 2: close iterator
        close = true;
      }
    } catch(final QueryException ex) {
      // exception may occur during qp.init() or qp.next()
      close = true;

      // log exception (static or runtime)
      final String msg = ex.extended();
      log.write(this, arg, INFOERROR + msg);
      // send 0 to mark end of potential result, 1 as error flag, and {MSG}0
      out.write(0);
      out.write(1);
      out.writeString(msg);
    }
    out.flush();

    // close query process after last item, close command or exception
    if(close && qp != null) {
      qp.close();
      queries.remove(arg);
    }
  }

  /**
   * Sends a success flag (0 = true, 1 = false) to the client.
   * @param ok success flag
   * @throws IOException I/O exception
   */
  private void send(final boolean ok) throws IOException {
    out.write(ok ? 0 : 1);
    out.flush();
  }

  /**
   * Exits the session.
   */
  public void exit() {
    // close remaining query processes
    for(final QueryProcess q : queries.values()) {
      try { q.close(); } catch(final IOException ex) { }
    }

    new Close().exec(context);
    if(proc != null) proc.stop();
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
