package org.basex.server;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * This class writes daily log files to disk.
 * The log format has been updated in Version 7.4; it now has the following columns:
 * <ul>
 *   <li><b>Time</b>: timestamp (format: {@code xs:time})</li>
 *   <li><b>Address</b>: host name and port of the requesting client</li>
 *   <li><b>User</b>: user name</li>
 *   <li><b>Type</b>: Type of log message: REQUEST, OK or ERROR</li>
 *   <li><b>Info</b>: Log message</li>
 *   <li><b>Performance</b>: Measured time in milliseconds</li>
 * </ul>
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Log implements QueryTracer {
  /** Server string. */
  public static final String SERVER = "SERVER";
  /** Log types. */
  public enum LogType {
    /** Request. */ REQUEST,
    /** Trace.   */ TRACE,
    /** Info.    */ INFO,
    /** Error.   */ ERROR,
    /** OK.      */ OK
  }

  /** Static options. */
  private final StaticOptions sopts;

  /** Current log file. */
  private LogFile file;

  /**
   * Constructor.
   * @param sopts static options
   */
  public Log(final StaticOptions sopts) {
    this.sopts = sopts;
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
  private void write(final String type, final String info, final Performance perf,
      final String address, final String user) {

    // check if logging is disabled
    if(!sopts.get(StaticOptions.LOG)) return;

    // construct log text
    final Date date = new Date();
    final int ml = sopts.get(StaticOptions.LOGMSGMAXLEN);
    final TokenBuilder tb = new TokenBuilder();
    tb.add(DateTime.format(date, DateTime.TIME));
    tb.add('\t').add(address != null ? address.replaceFirst("^/", "") : SERVER);
    tb.add('\t').add(user != null ? user : UserText.ADMIN);
    tb.add('\t').add(type);
    tb.add('\t').add(info != null ? chop(normalize(token(info)), ml) : EMPTY);
    if(perf != null) tb.add('\t').add(perf);
    tb.add(Prop.NL);

    try {
      synchronized(sopts) {
        // create new log file and write log entry
        final String name = DateTime.format(date, DateTime.DATE);
        if(file != null && !file.valid(name)) close();
        if(file == null) file = LogFile.create(name, dir());
        // write log entry
        file.write(tb.finish());
      }
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  /**
   * Closes the log file.
   */
  public void close() {
    try {
      synchronized(sopts) {
        if(file != null) {
          file.close();
          file = null;
        }
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
