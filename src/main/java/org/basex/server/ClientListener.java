package org.basex.server;

import static org.basex.core.Text.*;
import static org.basex.server.ServerCmd.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import org.basex.core.BaseXException;
import org.basex.core.Command;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.core.MainProp;
import org.basex.core.User;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Exit;
import org.basex.core.cmd.Replace;
import org.basex.core.cmd.Store;
import org.basex.io.in.BufferInput;
import org.basex.io.in.DecodingInput;
import org.basex.io.out.EncodingOutput;
import org.basex.io.out.PrintOutput;
import org.basex.query.QueryException;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.basex.util.list.ByteList;
import org.basex.util.list.StringList;

/**
 * Server-side client session in the client-server architecture.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
public final class ClientListener extends Thread {
  /** Active queries. */
  private final HashMap<String, QueryListener> queries =
    new HashMap<String, QueryListener>();
  /** Performance measurement. */
  final Performance perf = new Performance();
  /** Database context. */
  private final Context context;
  /** Socket reference. */
  private final Socket socket;
  /** Log reference. */
  private final Log log;

  /** Socket for events. */
  private Socket esocket;
  /** Output for events. */
  private PrintOutput eout;
  /** Active events. */
  private StringList events;
  /** Input stream. */
  private BufferInput in;
  /** Output stream. */
  private PrintOutput out;
  /** Current command. */
  private Command command;
  /** Query id counter. */
  private int id;
  /** Indicates if the server thread is running. */
  private boolean running;

  /**
   * Constructor.
   * @param s socket
   * @param c database context
   * @param l log reference
   */
  public ClientListener(final Socket s, final Context c, final Log l) {
    context = new Context(c, this);
    socket = s;
    log = l;
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
      running = context.user != null &&
        md5(string(context.user.password) + ts).equals(pw);

      // write log information
      if(running) {
        log.write(this, "LOGIN " + context.user.name, OK);
        // send {OK}
        send(true);
        // start listener thread
        start();
      } else if(!us.isEmpty()) {
        log.write(this, SERVERDENIED + COLS + us);
      }
      // return result flag
      return running;
    } catch(final IOException ex) {
      Util.stack(ex);
      log.write(ex.getMessage());
      return false;
    }
  }

  @Override
  public void run() {
    ServerCmd sc = null;
    String cmd = null;

    try {
      while(running) {
        try {
          final int b = in.read();
          if(b == -1) {
            // end of stream: exit session
            exit();
            break;
          }

          perf.getTime();
          sc = get(b);
          cmd = null;
          if(sc == CREATE) {
            create();
          } else if(sc == ADD) {
            add();
          } else if(sc == WATCH) {
            watch();
          } else if(sc == UNWATCH) {
            unwatch();
          } else if(sc == REPLACE) {
            replace();
          } else if(sc == STORE) {
            store();
          } else if(sc != COMMAND) {
            query(sc);
          } else {
            // database command
            cmd = new ByteList().add(b).add(in.readBytes()).toString();
          }
        } catch(final IOException ex) {
          // this exception may be thrown if a session is stopped
          exit();
          break;
        }
        if(sc != COMMAND) continue;

        // parse input and create command instance
        command = null;
        try {
          command = new CommandParser(cmd, context).parseSingle();
        } catch(final QueryException ex) {
          // log invalid command
          final String msg = ex.getMessage();
          log.write(this, cmd, INFOERROR + msg);
          // send 0 to mark end of potential result
          out.write(0);
          // send {INFO}0
          out.writeString(msg);
          // send 1 to mark error
          send(false);
          continue;
        }

        // start timeout
        command.startTimeout(context.mprop.num(MainProp.TIMEOUT));
        log.write(this,
            command.toString().replace('\r', ' ').replace('\n', ' '));

        // execute command and send {RESULT}
        boolean ok = true;
        String info = null;
        try {
          command.execute(context, new EncodingOutput(out));
          info = command.info();
        } catch(final BaseXException ex) {
          ok = false;
          info = ex.getMessage();
          if(info.startsWith(PROGERR)) info = SERVERTIMEOUT;
        }
        // stop timeout
        command.stopTimeout();

        // send 0 to mark end of result
        out.write(0);
        // send info
        info(info, ok);

        // stop console
        if(command instanceof Exit) {
          exit();
          break;
        }
      }
    } catch(final IOException ex) {
      log.write(this, sc == COMMAND ? cmd : sc, INFOERROR + ex.getMessage());
      Util.stack(ex);
      exit();
    }
  }

  /**
   * Exits the session.
   */
  public synchronized void exit() {
    running = false;
    try {
      // remove this session from all events in pool
      if(events != null) {
        esocket.close();
        for(final String e : events) {
          final Sessions sess = context.events.get(e);
          if(sess != null) sess.remove(this);
        }
      }
      new Close().execute(context);
      if(command != null) command.stop();
      context.delete(this);
      socket.close();
      log.write(this, "LOGOUT " + context.user.name, OK);
    } catch(final Exception ex) {
      log.write(ex.getMessage());
      Util.stack(ex);
    }
  }

  /**
   * Returns the user of this session.
   * @return user reference
   */
  public User user() {
    return context.user;
  }

  /**
   * Registers the event socket.
   * @param s socket
   * @throws IOException I/O exception
   */
  public synchronized void register(final Socket s) throws IOException {
    esocket = s;
    eout = PrintOutput.get(s.getOutputStream());
    eout.write(0);
    eout.flush();
  }

  /**
   * Sends a notification to the client.
   * @param name event name
   * @param msg event message
   * @throws IOException I/O exception
   */
  public synchronized void notify(final byte[] name, final byte[] msg)
      throws IOException {
    eout.print(name);
    eout.write(0);
    eout.print(msg);
    eout.write(0);
    eout.flush();
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder("[");
    tb.add(socket.getInetAddress().getHostAddress());
    tb.add(COL).addExt(socket.getPort()).add(']');
    if(context.data() != null) tb.add(COLS).add(context.data().meta.name);
    return tb.toString();
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Returns error feedback.
   * @param info error string
   * @throws IOException I/O exception
   */
  private void error(final String info) throws IOException {
    info(info, false);
  }

  /**
   * Returns user feedback.
   * @param info information string
   * @throws IOException I/O exception
   */
  private void success(final String info) throws IOException {
    info(info, true);
  }

  /**
   * Returns user feedback.
   * @param info information string
   * @param ok success/error flag
   * @throws IOException I/O exception
   */
  private void info(final String info, final boolean ok) throws IOException {
    // write feedback to log file
    log.write(this, ok ? OK : INFOERROR + info, perf);
    // send {MSG}0 and (0|1) as (success|error) flag
    out.writeString(info);
    send(ok);
  }

  /**
   * Creates a database.
   * @throws IOException I/O exception
   */
  private void create() throws IOException {
    execute(new CreateDB(in.readString()));
  }

  /**
   * Adds a document to a database.
   * @throws IOException I/O exception
   */
  private void add() throws IOException {
    execute(new Add(null, in.readString(), in.readString()));
  }

  /**
   * Replace a document in a database.
   * @throws IOException I/O exception
   */
  private void replace() throws IOException {
    execute(new Replace(in.readString()));
  }

  /**
   * Stores raw data in a database.
   * @throws IOException I/O exception
   */
  private void store() throws IOException {
    execute(new Store(in.readString()));
  }

  /**
   * Executes the specified command.
   * @param cmd command to be executed
   * @throws IOException I/O exception
   */
  private void execute(final Command cmd) throws IOException {
    log.write(this, cmd + "[...]");
    final DecodingInput di = new DecodingInput(in);
    try {
      cmd.setInput(di);
      cmd.execute(context);
      success(cmd.info());
    } catch(final BaseXException ex) {
      di.flush();
      error(ex.getMessage());
    }
  }

  /**
   * Watches an event.
   * @throws IOException I/O exception
   */
  private void watch() throws IOException {
    // initialize server-based event handling
    if(events == null) {
      out.writeString(Integer.toString(context.mprop.num(MainProp.EVENTPORT)));
      out.writeString(Long.toString(getId()));
      out.flush();
      events = new StringList();
    }
    final String name = in.readString();
    final Sessions s = context.events.get(name);
    final boolean ok = s != null && !s.contains(this);
    String message = "";
    if(ok) {
      s.add(this);
      events.add(name);
      message = EVENTWAT;
    } else if(s == null) {
      message = EVENTNO;
    } else {
      message = EVENTALR;
    }
    info(Util.info(message, name), ok);
  }

  /**
   * Unwatches an event.
   * @throws IOException I/O exception
   */
  private void unwatch() throws IOException {
    final String name = in.readString();

    final Sessions s = context.events.get(name);
    final boolean ok = s != null && s.contains(this);
    String message = "";
    if(ok) {
      s.remove(this);
      events.delete(name);
      message = EVENTUNWAT;
    } else if(s == null) {
      message = EVENTNO;
    } else {
      message = EVENTNOUW;
    }
    info(Util.info(message, name), ok);
    out.flush();
  }

  /**
   * Processes the query iterator.
   * @param sc server command
   * @throws IOException I/O exception
   */
  private void query(final ServerCmd sc) throws IOException {
    // iterator argument (query or identifier)
    String arg = in.readString();

    String err = null;
    try {
      final QueryListener qp;
      if(sc == QUERY) {
        final String query = arg;
        qp = new QueryListener(query, context);
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
        } else if(sc == ITER) {
          qp.execute(true, out, true);
        } else if(sc == EXEC) {
          qp.execute(false, out, true);
        } else if(sc == INFO) {
          out.print(qp.info());
        } else if(sc == OPTIONS) {
          out.print(qp.options());
        } else if(sc == CLOSE) {
          queries.remove(arg);
        } else if(sc == NEXT) {
          throw new Exception("Protocol for query iteration is out-of-dated.");
        }
        // send 0 as end marker
        out.write(0);
      }
      // send 0 as success flag
      out.write(0);
      // write log file (bind has been logged before)
      if(sc != BIND) log.write(this, sc + "(" + arg + ")", OK);
    } catch(final Exception ex) {
      // log exception (static or runtime)
      err = ex.getMessage();
      log.write(this, sc + "(" + arg + ")", INFOERROR + err);
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
   * Sends a success flag to the client (0: true, 1: false).
   * @param ok success flag
   * @throws IOException I/O exception
   */
  void send(final boolean ok) throws IOException {
    out.write(ok ? 0 : 1);
    out.flush();
  }
}
