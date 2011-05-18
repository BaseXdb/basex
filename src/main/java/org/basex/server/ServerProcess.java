package org.basex.server;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import static org.basex.server.ServerCmd.*;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import org.basex.build.Parser;
import org.basex.core.BaseXException;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.Commands.CmdCreate;
import org.basex.core.cmd.Add;
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
 * @author BaseX Team 2005-11, BSD License
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

      if(ok) {
        start();
      } else if(!us.isEmpty()) {
        // log failed login and delay feedback
        log.write(this, SERVERLOGIN + ": " + us);
        Performance.sleep(2000);
      }
      // send {OK}
      send(ok);
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
          final byte b = in.readByte();
          final ServerCmd sc = ServerCmd.get(b);
          if(sc == CREATE) {
            create();
            continue;
          }
          if(sc == ADD) {
            add();
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
          cmd = new CommandParser(input, context).parseSingle();
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

        final String c = cmd.toString().replace('\r', ' ').replace('\n', ' ');
        log.write(this, c);

        // execute command and send {RESULT}
        boolean ok = true;
        String info = null;
        try {
          cmd.execute(context, out);
          info = cmd.info();
        } catch(final BaseXException ex) {
          ok = false;
          info = ex.getMessage();
          if(info.startsWith(PROGERR)) info = SERVERTIMEOUT;
        }
        // stop timeout
        cmd.stopTimeout();

        // send 0 to mark end of result
        out.write(0);
        // send {INFO}0
        out.writeString(info);
        // send {OK}
        send(ok);

        log.write(this, ok ? OK : INFOERROR + info, perf);
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
    final Performance perf = new Performance();
    final String name = in.readString();
    final String str = ServerCmd.CREATE + " " +
      CmdCreate.DATABASE + " " + name + " [...]";
    log.write(this, str);

    try {
      final WrapInputStream is = new WrapInputStream(in);
      final String info = is.curr() == -1 ?
        CreateDB.xml(name, Parser.emptyParser(), context) :
        CreateDB.xml(name, is, context);
      // send {MSG}0 and 0 as success flag
      out.writeString(info);
      out.write(0);
      log.write(this, OK, perf);
    } catch(final BaseXException ex) {
      // send {MSG}0 and 1 as error flag
      out.writeString(ex.getMessage());
      out.write(1);
      log.write(this, INFOERROR + ex.getMessage(), perf);
    }
    out.flush();
  }

  /**
   * Adds a document to a database.
   * @throws IOException I/O exception
   */
  private void add() throws IOException {
    final Performance perf = new Performance();
    final String name = in.readString();
    final String path = in.readString();
    final StringBuilder sb = new StringBuilder(ServerCmd.ADD + " ");
    if(!name.isEmpty()) sb.append(AS + ' ' + name + ' ');
    if(!path.isEmpty()) sb.append(TO + ' ' + path + ' ');
    final String str = sb.append("[...]").toString();
    log.write(this, str);

    try {
      final WrapInputStream is = new WrapInputStream(in);
      final String info = Add.add(name, path, is, context, null);
      // send {MSG}0 and 0 as success flag
      out.writeString(info);
      out.write(0);
      log.write(this, OK, perf);
    } catch(final BaseXException ex) {
      // send {MSG}0 and 1 as error flag
      out.writeString(ex.getMessage());
      out.write(1);
      log.write(this, INFOERROR + ex.getMessage(), perf);
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
        final String query = arg;
        qp = new QueryProcess(query, out, context);
        arg = Integer.toString(id++);
        queries.put(arg, qp);
        // send {ID}0
        out.writeString(arg);

        // write log file
        log.write(this, sc + "(" + arg + ")", query, OK);
      } else {
        // find query process
        qp = queries.get(arg);

        // ID has already been removed
        if(qp == null) {
          if(sc != CLOSE) throw new IOException("Unknown Query ID: " + arg);
        } else if(sc == BIND) {
          final String key = in.readString();
          final String val = in.readString();
          final String typ = in.readString();
          qp.bind(key, val, typ);
          log.write(this, sc + "(" + arg + ")", key, val, typ, OK);
        } else if(sc == INIT) {
          qp.init();
        } else if(sc == NEXT) {
          qp.next();
        } else if(sc == EXEC) {
          qp.execute();
        } else if(sc == INFO) {
          qp.info();
        } else if(sc == CLOSE) {
          qp.close(false);
          queries.remove(arg);
        }
        // send 0 as end marker
        out.write(0);
      }
      // send 0 as success flag
      out.write(0);

      // write log file (skip next calls; bind has been logged before)
      if(sc != NEXT && sc != BIND) log.write(this, sc + "(" + arg + ")", OK);
    } catch(final Exception ex) {
      // log exception (static or runtime)
      err = ex.getMessage();
      log.write(this, sc + "(" + arg + ")", INFOERROR + err);

      if(qp != null) qp.close(true);
      queries.remove(arg);
    }
    if(err != null) {
      // send 0 as end marker, 1 as error flag, and {MSG}0
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
