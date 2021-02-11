package org.basex.server;

import static org.basex.core.Text.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.parse.*;
import org.basex.core.users.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.server.Log.LogType;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Server-side client session in the client-server architecture.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
public final class ClientListener extends Thread implements ClientInfo {
  /** Prints trace output to the evaluation info. */
  private static final QueryTracer PASS = info -> true;

  /** Timer for authentication time out. */
  public final Timer timeout = new Timer();
  /** Timestamp of last interaction. */
  public long last;

  /** Active queries. */
  private final HashMap<String, ServerQuery> queries = new HashMap<>();
  /** Performance measurement. */
  private final Performance perf = new Performance();
  /** Database context. */
  private final Context context;
  /** Server reference. */
  private final BaseXServer server;
  /** Socket reference. */
  private final Socket socket;

  /** Input stream. */
  private BufferInput in;
  /** Output stream. */
  private PrintOutput out;
  /** Current command. */
  private Command command;
  /** Query id counter. */
  private int id;
  /** Indicates if the server thread is running. */
  private volatile boolean authenticated;
  /** Indicates if the server has been closed. */
  private boolean closed;

  /**
   * Constructor.
   * @param socket socket
   * @param context database context
   * @param server server reference
   */
  public ClientListener(final Socket socket, final Context context, final BaseXServer server) {
    this.context = new Context(context, this);
    this.socket = socket;
    this.server = server;
    last = System.currentTimeMillis();
    setDaemon(true);
  }

  @Override
  public void run() {
    if(!authenticate()) return;

    try {
      while(authenticated) {
        command = null;
        String cmd;
        final ServerCmd sc;
        try {
          final int b = in.read();
          if(b == -1) {
            // end of stream: exit session
            close();
            break;
          }

          last = System.currentTimeMillis();
          perf.ns();
          sc = ServerCmd.get(b);
          cmd = null;
          if(sc == ServerCmd.CREATE) {
            create();
          } else if(sc == ServerCmd.ADD) {
            add();
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
          Util.debug(ex);
          close();
          break;
        }
        if(sc != ServerCmd.COMMAND) continue;

        // parse input and create command instance
        try {
          command = CommandParser.get(cmd, context).parseSingle();
          command.jc().tracer = PASS;
          log(LogType.REQUEST, command.toString(true));
        } catch(final QueryException ex) {
          // log invalid command
          final String msg = ex.getMessage();
          log(LogType.REQUEST, cmd);
          log(LogType.ERROR, msg);
          // send 0 to mark end of potential result
          out.write(0);
          // send {INFO}0
          out.print(msg);
          out.write(0);
          // send 1 to mark error
          send(false);
          continue;
        }

        // execute command and send {RESULT}
        boolean ok = true;
        String info;
        try {
          // run command
          command.execute(context, new ServerOutput(out));
          info = command.info();
        } catch(final BaseXException ex) {
          Util.debug(ex);
          ok = false;
          info = ex.getMessage();
        }

        // send 0 to mark end of result
        out.write(0);
        // send info
        info(info, ok);

        // stop console
        if(command instanceof Exit) {
          command = null;
          close();
        }
      }
    } catch(final IOException ex) {
      log(LogType.ERROR, Util.message(ex));
      command = null;
      close();
    }
    command = null;
  }

  /**
   * Initializes a session via digest authentication.
   * @return success flag
   */
  private boolean authenticate() {
    boolean auth = false;
    try {
      final String nonce = Long.toString(System.nanoTime());
      final byte[] address = socket.getInetAddress().getAddress();

      // send {REALM:TIMESTAMP}0
      out = PrintOutput.get(socket.getOutputStream());
      out.print(Prop.NAME + ':' + nonce);
      send(true);

      // evaluate login data
      in = BufferInput.get(socket.getInputStream());
      // receive {USER}0{DIGEST-HASH}0
      final String name = in.readString(), hash = in.readString();
      final User user = context.users.get(name);
      auth = user != null &&
          Strings.md5(user.code(Algorithm.DIGEST, Code.HASH) + nonce).equals(hash);

      // write log information
      if(auth) {
        context.user(user);
        // send {OK}
        send(true);
        context.blocker.remove(address);
        context.sessions.add(this);
      } else {
        if(!name.isEmpty()) log(LogType.ERROR, Util.info(ACCESS_DENIED_X, name));
        // delay users with wrong passwords
        context.blocker.delay(address);
        send(false);
      }
    } catch(final IOException ex) {
      if(auth) {
        Util.stack(ex);
        log(LogType.ERROR, Util.message(ex));
        auth = false;
      }
    }

    server.remove(this);
    authenticated = auth;
    return auth;
  }

  /**
   * Closes the session.
   */
  public synchronized void close() {
    if(closed) return;
    closed = true;

    // stop running command, wait until reference has been invalidated
    final Command c = command;
    if(c != null) {
      c.stop();
      do Performance.sleep(1); while(command != null);
    }
    context.sessions.remove(this);

    try {
      if(context.user() != null) Close.close(context);
      socket.close();
    } catch(final Throwable ex) {
      log(LogType.ERROR, Util.message(ex));
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

  @Override
  public String clientName() {
    final User user = context.user();
    return user != null ? user.name() : null;
  }

  @Override
  public String clientAddress() {
    return socket.getInetAddress().getHostAddress() + ':' + socket.getPort();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("[").append(clientAddress()).append(']');
    if(context.data() != null) sb.append(COLS).append(context.data().meta.name);
    return sb.toString();
  }

  // PRIVATE METHODS ==============================================================================

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
    log(ok ? LogType.OK : LogType.ERROR, info);
    // send {MSG}0 and (0|1) as (success|error) flag
    out.print(info);
    out.write(0);
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
    log(LogType.REQUEST, cmd + " [...]");
    final ServerInput si = new ServerInput(in);
    try {
      cmd.setInput(si);
      cmd.execute(context);
      success(cmd.info());
    } catch(final BaseXException ex) {
      si.flush();
      error(ex.getMessage());
    }
  }

  /**
   * Processes the query iterator.
   * @param sc server command
   * @throws IOException I/O exception
   */
  private void query(final ServerCmd sc) throws IOException {
    // iterator argument (query or identifier)
    String arg = in.readString();

    String error = null;
    try {
      final ServerQuery qp;
      final StringBuilder info = new StringBuilder();
      if(sc == ServerCmd.QUERY) {
        final String query = arg;
        qp = new ServerQuery(query, context);
        qp.jc().tracer = PASS;
        arg = Integer.toString(id++);
        queries.put(arg, qp);
        // send {ID}0
        out.print(arg);
        out.write(0);
        // write log file
        info.append(query);
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
          info.append(key).append('=').append(val);
          if(!typ.isEmpty()) info.append(" as ").append(typ);
        } else if(sc == ServerCmd.CONTEXT) {
          final String val = in.readString();
          final String typ = in.readString();
          qp.context(val, typ);
          info.append(val);
          if(!typ.isEmpty()) info.append(" as ").append(typ);
        } else if(sc == ServerCmd.RESULTS) {
          qp.execute(out, true, true, false);
        } else if(sc == ServerCmd.EXEC) {
          qp.execute(out, false, true, false);
        } else if(sc == ServerCmd.FULL) {
          qp.execute(out, true, true, true);
        } else if(sc == ServerCmd.INFO) {
          out.print(qp.info());
        } else if(sc == ServerCmd.OPTIONS) {
          out.print(qp.parameters());
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
      // write log file
      log(LogType.OK, sc.toString() + '[' + arg + "] " + info);

    } catch(final Throwable ex) {
      // log exception (static or runtime)
      error = ex instanceof RuntimeException ? Util.bug(ex) : Util.message(ex);
      log(LogType.REQUEST, sc + "[" + arg + ']');
      log(LogType.ERROR, error);
      queries.remove(arg);
    }
    if(error != null) {
      // send 0 as end marker, 1 as error flag, and {MSG}0
      out.write(0);
      out.write(1);
      out.print(error);
      out.write(0);
    }
    out.flush();
  }

  /**
   * Sends a success flag to the client (0: true, 1: false).
   * @param ok success flag
   * @throws IOException I/O exception
   */
  private void send(final boolean ok) throws IOException {
    out.write(ok ? 0 : 1);
    out.flush();
  }

  /**
   * Writes a log message.
   * @param type log type
   * @param info message info
   */
  private void log(final LogType type, final String info) {
    context.log.write(type, info, perf, context);
  }
}
