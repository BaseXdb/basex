package org.basex.util.log;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * This class processes log entries. The log format:
 * <ul>
 *   <li><b>Time</b>: timestamp (format: {@code xs:time})</li>
 *   <li><b>Address</b>: host name and port of the requesting client</li>
 *   <li><b>User</b>: username</li>
 *   <li><b>Type</b>: Type of log message: REQUEST, OK or ERROR</li>
 *   <li><b>Info</b>: Log message</li>
 *   <li><b>Performance</b>: Measured time in milliseconds</li>
 * </ul>
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class Log implements QueryTracer {
  /** Server string. */
  private static final String SERVER = "SERVER";

  /** Static options. */
  private final StaticOptions sopts;
  /** Maximum length of log messages. */
  private final int maxLen;

  /** Log targets. */
  private Set<LogTarget> targets;
  /** Current (daily) log file. */
  LogFile file;

  /**
   * Constructor.
   * @param sopts static options
   */
  public Log(final StaticOptions sopts) {
    this.sopts = sopts;
    maxLen = sopts.get(StaticOptions.LOGMSGMAXLEN);
  }

  /**
   * Returns a log file for the specified name (current or new instance).
   * @param name name of log file
   * @return log file, or {@code null} if it does not exist
   */
  public LogFile file(final String name) {
    LogFile lf = file;
    if(lf == null || !lf.valid(name)) lf = new LogFile(name, dir());
    return lf.exists() ? lf : null;
  }

  /**
   * Writes a server entry to the log file.
   * @param type log type
   * @param info info string (can be {@code null})
   */
  public void writeServer(final LogType type, final String info) {
    write(type.toString(), info, null, null, null);
  }

  /**
   * Writes an entry to the log file.
   * @param type type
   * @param info info string (can be {@code null})
   * @param perf performance object (can be {@code null})
   * @param ctx database context
   */
  public void write(final LogType type, final String info, final Performance perf,
      final Context ctx) {
    write(type.toString(), info, perf, ctx);
  }

  /**
   * Writes an entry to the log file.
   * @param type type
   * @param info info string (can be {@code null})
   * @param perf performance object (can be {@code null})
   * @param address address (can be {@code null})
   * @param ctx database context
   */
  public void write(final LogType type, final String info, final Performance perf,
      final String address, final Context ctx) {
    write(type.toString(), info, perf, address, ctx.clientName());
  }

  /**
   * Writes an entry to the log file.
   * @param type type ({@link LogType}, HTTP status code or custom string)
   * @param info info string (can be {@code null})
   * @param perf performance object (can be {@code null})
   * @param ctx database context
   */
  public void write(final Object type, final String info, final Performance perf,
      final Context ctx) {
    write(type.toString(), info, perf, ctx.clientAddress(), ctx.clientName());
  }

  /**
   * Writes an entry to the log file.
   * @param type type ({@link LogType}, HTTP status code or custom string)
   * @param info info string (can be {@code null})
   * @param perf performance object (can be {@code null})
   * @param address address string ({@code SERVER} is written if value is {@code null})
   * @param user user ({@code admin} is written if value is {@code null})
   */
  private synchronized void write(final String type, final String info, final Performance perf,
      final String address, final String user) {

    if(skip()) return;

    final LogEntry entry = new LogEntry();
    entry.log = this;
    entry.date = new Date();
    entry.time = DateTime.format(entry.date, DateTime.TIME);
    entry.address = address != null ? address.replaceFirst("^/", "") : SERVER;
    entry.user = user != null ? user : UserText.ADMIN;
    entry.type = type;
    final String inf = info != null ? info : "";
    final int len = inf.codePointCount(0, inf.length());
    entry.info = len > maxLen ? inf.substring(0, inf.offsetByCodePoints(0, maxLen)) + "..." : inf;
    if(perf != null) entry.runtime = perf.toString();

    // write log entry to requested targets
    for(final LogTarget target : targets) {
      try {
        target.write(this, entry);
      } catch(final IOException ex) {
        Util.stack(ex);
      }
    }
  }

  /**
   * Checks if logging is requested. Initializes the log targets if not done yet
   * (not part of the constructor to consider command-line arguments).
   * @return result of check
   */
  private boolean skip() {
    if(targets == null) {
      final String log = sopts.get(StaticOptions.LOG);
      final Set<LogTarget> set = EnumSet.noneOf(LogTarget.class);
      for(final String target : log.trim().toUpperCase().split("\\s*,\\s*")) {
        final Boolean enable = Strings.toBoolean(target);
        if(enable == null) {
          for(final LogTarget lt : LogTarget.values()) {
            if(target.equals(lt.name())) set.add(lt);
          }
        } else if(enable) {
          set.add(LogTarget.DATA);
        } else {
          set.clear();
          break;
        }
      }
      targets = set;
    }
    return targets.isEmpty();
  }

  /**
   * Closes the log file.
   */
  public synchronized void close() {
    try {
      if(file != null) {
        file.close();
        file = null;
      }
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  /**
   * Returns all log files.
   * @return log directory
   */
  public IOFile[] files() {
    return dir().children(".*\\" + IO.LOGSUFFIX);
  }

  /**
   * Writes an entry to the daily database log file.
   * @param entry log entry
   * @throws IOException I/O exception
   */
  public void write(final LogEntry entry) throws IOException {
    final String name = DateTime.format(entry.date, DateTime.DATE);
    if(file != null && !file.valid(name)) close();
    if(file == null) file = LogFile.create(name, dir());
    file.write(Token.token(entry + Prop.NL));
  }

  /**
   * Returns a reference to the log directory.
   * @return log directory
   */
  private IOFile dir() {
    return sopts.dbPath(".").resolve(sopts.get(StaticOptions.LOGPATH));
  }

  @Override
  public boolean print(final String info) {
    writeServer(LogType.TRACE, info);
    return false;
  }
}
