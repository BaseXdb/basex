package org.basex.server;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.parse.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Server-side client session in the client-server architecture.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
public final class ClientListener extends Thread {
  /** Timer for authentication time out. */
  public final Timer auth = new Timer();
  /** Timestamp of last interaction. */
  public long last;
  /** Log reference. */
  final Log log;

  /** Active queries. */
  private final HashMap<String, QueryListener> queries =
    new HashMap<String, QueryListener>();
  /** Performance measurement. */
  private final Performance perf = new Performance();
  /** Database context. */
  private final Context context;
  /** Server reference. */
  private final BaseXServer server;
  /** Socket reference. */
  private final Socket socket;

  /** Socket for events. */
  private Socket esocket;
  /** Output for events. */
  private PrintOutput eout;
  /** Flag for active events. */
  private boolean events;
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
   * @param srv server reference
   */
  public ClientListener(final Socket s, final Context c, final Log l,
      final BaseXServer srv) {

    context = new Context(c, this);
    socket = s;
    log = l;
    server = srv;
    last = System.currentTimeMillis();
  }

  @Override
  public void run() {
    if(!authenticate()) return;

    ServerCmd sc = null;
    String cmd = null;
    try {
      while(running) {
        command = null;
        try {
          final int b = in.read();
          if(b == -1) {
            // end of stream: exit session
            quit();
            break;
          }

          last = System.currentTimeMillis();
          perf.time();
          sc = ServerCmd.get(b);
          cmd = null;
          if(sc == ServerCmd.CREATE) {
            create();
          } else if(sc == ServerCmd.ADD) {
            add();
          } else if(sc == ServerCmd.WATCH) {
            watch();
          } else if(sc == ServerCmd.UNWATCH) {
            unwatch();
          } else if(sc == ServerCmd.REPLACE) {
            replace();
          } else if(sc == ServerCmd.STORE) {
            store();
          } else if(sc != ServerCmd.COMMAND) {
            query(sc);
          } else {
            // database command
            cmd = new ByteList().add(b).add(in.readBytes()).toString();
          }
        } catch(final IOException ex) {
          // this exception may be thrown if a session is stopped
          quit();
          break;
        }
        if(sc != ServerCmd.COMMAND) continue;

        // parse input and create command instance
        try {
          command = new CommandParser(cmd, context).parseSingle();
        } catch(final QueryException ex) {
          // log invalid command
          final String msg = ex.getMessage();
          log.write(this, cmd, ERROR_C + msg);
          // send 0 to mark end of potential result
          out.write(0);
          // send {INFO}0
          out.writeString(msg);
          // send 1 to mark error
          send(false);
          continue;
        }

        log.write(this, command.toString().replace('\r', ' ').replace('\n', ' '));

        // execute command and send {RESULT}
        boolean ok = true;
        String info;
        try {
          // run command
          command.execute(context, new EncodingOutput(out));
          info = command.info();
        } catch(final BaseXException ex) {
          ok = false;
          info = ex.getMessage();
          if(info.startsWith(INTERRUPTED)) info = TIMEOUT_EXCEEDED;
        }

        // send 0 to mark end of result
        out.write(0);
        // send info
        info(info, ok);

        // stop console
        if(command instanceof Exit) {
          command = null;
          quit();
        }
      }
    } catch(final IOException ex) {
      log.write(this, sc == ServerCmd.COMMAND ? cmd : sc, ERROR_C + ex.getMessage());
      Util.debug(ex);
      command = null;
      quit();
    }
    command = null;
  }

  /**
   * Initializes a session via cram-md5.
   * @return success flag
   */
  private boolean authenticate() {
    try {
      final String ts = Long.toString(System.nanoTime());
      final byte[] address = socket.getInetAddress().getAddress();

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
      running = context.user != null && md5(context.user.password + ts).equals(pw);

      // write log information
      if(running) {
        log.write(this, "LOGIN " + context.user.name, OK);
        // send {OK}
        send(true);
        server.unblock(address);
        context.add(this);
      } else {
        if(!us.isEmpty()) log.write(this, ACCESS_DENIED + COLS + us);
        new ClientDelayer(server.block(address), this, server).start();
      }
    } catch(final IOException ex) {
      if(running) {
        Util.stack(ex);
        log.write(ex.getMessage());
        running = false;
      }
    }

    server.remove(this);
    return running;
  }

  /**
   * Quits the authentication.
   */
  public synchronized void quitAuth() {
    try {
      socket.close();
      log.write(this, ERROR_C + TIMEOUT_EXCEEDED);
    } catch(final Throwable ex) {
      log.write(this, ex.getMessage());
    }
  }

  /**
   * Exits the session.
   */
  public synchronized void quit() {
    if(!running) return;

    running = false;
    if(log != null) log.write(this, "LOGOUT " +
        (null != context.user ? context.user.name : ""), OK);

    // wait until running command was stopped
    if(command != null) {
      command.stop();
      do Performance.sleep(50); while(command != null);
    }
    context.delete(this);

    try {
      new Close().run(context);
      socket.close();
      if(events) {
        esocket.close();
        // remove this session from all events in pool
        for(final Sessions s : context.events.values()) s.remove(this);
      }
    } catch(final Throwable ex) {
      if(log != null) log.write(this, ex.getMessage());
      Util.stack(ex);
    }
  }

  /**
   * Returns the context of this session.
   * @return user reference
   */
  public Context context() {
    return context;
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

    last = System.currentTimeMillis();
    eout.print(name);
    eout.write(0);
    eout.print(msg);
    eout.write(0);
    eout.flush();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("[");
    sb.append(socket.getInetAddress().getHostAddress());
    sb.append(COL).append(socket.getPort()).append(']');
    if(context.data() != null) sb.append(COLS).append(context.data().meta.name);
    return sb.toString();
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
    log.write(this, ok ? OK : ERROR_C + info, perf);
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
    execute(new Add(in.readString()));
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
    log.write(this, cmd + " [...]");
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
    server.initEvents();

    // initialize server-based event handling
    if(!events) {
      out.writeString(Integer.toString(context.mprop.num(MainProp.EVENTPORT)));
      out.writeString(Long.toString(getId()));
      out.flush();
      events = true;
    }
    final String name = in.readString();
    final Sessions s = context.events.get(name);
    final boolean ok = s != null && !s.contains(this);
    final String message;
    if(ok) {
      s.add(this);
      message = WATCHING_EVENT_X;
    } else if(s == null) {
      message = EVENT_UNKNOWN_X;
    } else {
      message = EVENT_WATCHED_X;
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
    final String message;
    if(ok) {
      s.remove(this);
      message = UNWATCHING_EVENT_X;
    } else if(s == null) {
      message = EVENT_UNKNOWN_X;
    } else {
      message = EVENT_NOT_WATCHED_X;
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
      if(sc == ServerCmd.QUERY) {
        final String query = arg;
        qp = new QueryListener(query, context);
        arg = Integer.toString(id++);
        queries.put(arg, qp);
        // send {ID}0
        out.writeString(arg);
        // write log file
        log.write(this, sc + "(" + arg + ')', query, OK, perf);
      } else {
        // find query process
        qp = queries.get(arg);

        // ID has already been removed
        if(qp == null) {
          if(sc != ServerCmd.CLOSE) throw new IOException("Unknown Query ID: " + arg);
        } else if(sc == ServerCmd.BIND) {
          final String key = in.readString();
          final String val = in.readString();
          final String typ = in.readString();
          qp.bind(key, val, typ);
          log.write(this, sc + "(" + arg + ')', key, val, typ, OK, perf);
        } else if(sc == ServerCmd.CONTEXT) {
          final String val = in.readString();
          final String typ = in.readString();
          qp.context(val, typ);
          log.write(this, sc + "(" + arg + ')', val, typ, OK, perf);
        } else if(sc == ServerCmd.ITER) {
          qp.execute(true, out, true, false);
        } else if(sc == ServerCmd.EXEC) {
          qp.execute(false, out, true, false);
        } else if(sc == ServerCmd.FULL) {
          qp.execute(true, out, true, true);
        } else if(sc == ServerCmd.INFO) {
          out.print(qp.info());
        } else if(sc == ServerCmd.OPTIONS) {
          out.print(qp.options());
        } else if(sc == ServerCmd.UPDATING) {
          out.print(Boolean.toString(qp.updating()));
        } else if(sc == ServerCmd.CLOSE) {
          queries.remove(arg);
        } else if(sc == ServerCmd.NEXT) {
          throw new Exception("Protocol for query iteration is out-of-date.");
        }
        // send 0 as end marker
        out.write(0);
      }
      // send 0 as success flag
      out.write(0);
      // write log file (bind and execute have been logged before)
      if(sc != ServerCmd.BIND && sc != ServerCmd.CONTEXT) {
        log.write(this, sc + "(" + arg + ')', OK, perf);
      }
    } catch(final Throwable ex) {
      // log exception (static or runtime)
      err = ex.getMessage();
      log.write(this, sc + "(" + arg + ')', ERROR_C + err);
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
