package org.basex.server;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import static org.basex.server.ServerCmd.*;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import org.basex.core.BaseXException;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Exit;
import org.basex.data.Data;
import org.basex.io.BufferInput;
import org.basex.io.PrintOutput;
import org.basex.io.WrapInputStream;
import org.basex.query.QueryException;
import org.basex.util.ByteList;
import org.basex.util.Performance;
import org.basex.util.Util;

/**
 * Single session for a client-server connection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
public final class ServerProcess extends Thread {
  /** Active queries. */
  private final HashMap<String, QueryProcess> queries =
    new HashMap<String, QueryProcess>();

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
  /** Current command. */
  private Command cmd;
  /** Query id counter. */
  private int id;
  /** Running of thread. */
  private boolean running;

  /**
   * Constructor.
   * @param s socket
   * @param c database context
   * @param l log reference
   */
  public ServerProcess(final Socket s, final Context c, final Log l) {
    context = new Context(c);
    log = l;
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
      out = PrintOutput.get(socket.getOutputStream());
      out.print(ts);
      send(true);

      // evaluate login data
      in = new BufferInput(socket.getInputStream());
      // receive {USER}0{PASSWORD}0
      final String us = in.readString();
      final String pw = in.readString();
      context.user = context.users.get(us);
      final boolean ok = context.user != null &&
        md5(string(context.user.password) + ts).equals(pw);
      // send {OK}
      send(ok);

      if(ok) start();
      else if(!us.isEmpty()) log.write(this, "LOGIN " + us, "failed");
      return ok;
    } catch(final IOException ex) {
      Util.stack(ex);
      log.write(ex.getMessage());
      return false;
    }
  }

  @Override
  public void run() {
    log.write(this, "LOGIN " + context.user.name, OK);
    String input = null;
    running = true;
    try {
      while(running) {
        try {
          // [CG] Server: replace control codes with strings.
          //final ServerCmd sc = ServerCommand.valueOf(in.readString());
          final byte b = in.readByte();
          final ServerCmd sc = ServerCmd.get(b);
          if(sc == CREATE) {
            create();
            continue;
          }
          if(sc != CMD) {
            query(sc);
            continue;
          }

          // database command
          input = new ByteList().add(b).add(in.content().toArray()).toString();
        } catch(final IOException ex) {
          // this exception is thrown for each session if the server is stopped
          exit();
          break;
        }

        // parse input and create command instance
        final Performance perf = new Performance();
        cmd = null;
        try {
          cmd = new CommandParser(input, context, true).parseSingle();
        } catch(final QueryException ex) {
          // log invalid command
          final String msg = ex.getMessage();
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
        if(cmd instanceof Exit) {
          exit();
          running = false;
          break;
        }

        // start timeout
        cmd.startTimeout(context.prop.num(Prop.TIMEOUT));

        // execute command and send {RESULT}
        boolean ok = true;
        String info = null;
        try {
          cmd.execute(context, out);
          info = cmd.info();
        } catch(final BaseXException ex) {
          ok = false;
          info = ex.getMessage();
          if(info.equals(PROGERR)) info = SERVERTIMEOUT;
        }
        // stop timeout
        cmd.stopTimeout();

        // send 0 to mark end of result
        out.write(0);
        // send {INFO}0
        out.writeString(info);
        // send {OK}
        send(ok);

        final String c = cmd.toString().replace('\r', ' ').replace('\n', ' ');
        log.write(this, c, ok ? OK : INFOERROR + info, perf);
      }
      if(!running) log.write(this, "LOGOUT " + context.user.name, OK);
    } catch(final IOException ex) {
      log.write(this, input, INFOERROR + ex.getMessage());
      Util.stack(ex);
      exit();
    }
  }

  /**
   * Creates a database.
   * @throws IOException I/O exception
   */
  private void create() throws IOException {
    try {
      final String name = in.readString();
      final WrapInputStream is = new WrapInputStream(in);
      // send {MSG}0 and 0 as success flag
      final String info = CreateDB.xml(name, is, context);
      out.writeString(info);
      out.write(0);
    } catch(final BaseXException ex) {
      // send {MSG}0 and 1 as error flag
      out.writeString(ex.getMessage());
      out.write(1);
    }
    out.flush();
  }

  /**
   * Processes the query iterator.
   * @param sc server command
   * @throws IOException I/O exception
   */
  private void query(final ServerCmd sc) throws IOException {
    // iterator argument
    String arg = in.readString();

    QueryProcess qp = null;
    String err = null;
    try {
      if(sc == QUERY) {
        qp = new QueryProcess(arg, out, context);
        arg = Integer.toString(id++);
        queries.put(arg, qp);

        log.write(this, arg, qp.query, OK);
        // send {ID}0
        out.writeString(arg);
      } else {
        // find query process
        qp = queries.get(arg);
        // avoid multiple close calls
        if(qp == null && sc != CLOSE)
          throw new BaseXException("Unknown query ID (" + arg + ")");

        if(sc == BIND) {
          qp.bind(in.readString(), in.readString(), in.readString());
        } else if(sc == INIT) {
          qp.init();
        } else if(sc == NEXT) {
          qp.next();
        } else if(sc == CLOSE && qp != null) {
          qp.close(false);
          queries.remove(arg);
        }
        // send 0 as end marker
        out.write(0);
      }
      // send 0 as success flag
      out.write(0);
    } catch(final BaseXException ex) {
      err = ex.getMessage();
    } catch(final QueryException ex) {
      // log exception (static or runtime)
      err = ex.getMessage();
      qp.close(true);
      queries.remove(arg);
    }
    if(err != null) {
      // send 0 as end marker, 1 as error flag, and {MSG}0
      log.write(this, arg, INFOERROR + err);
      out.write(0);
      out.write(1);
      out.writeString(err);
    }
    out.flush();
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
      try { q.close(true); } catch(final IOException ex) { }
    }

    try {
      new Close().execute(context);
      if(cmd != null) cmd.stop();
      context.delete(this);
      socket.close();
    } catch(final Exception ex) {
      log.write(ex.getMessage());
      Util.stack(ex);
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
    return Util.info("[%:%]", host, port);
  }
}
