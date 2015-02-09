package org.basex.server;

import static org.basex.util.Token.*;

import java.io.*;
import java.math.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.io.*;
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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class Log {
  /** Server string. */
  public static final String SERVER = "SERVER";
  /** Standalone string. */
  public static final String STANDALONE = "STANDALONE";
  /** Log types. */
  public enum LogType {
    /** Request. */ REQUEST,
    /** Info.    */ INFO,
    /** Error.   */ ERROR,
    /** OK.      */ OK
  }

  /** Static options. */
  private final StaticOptions sopts;
  /** Start date of log. */
  private String start;
  /** Output stream. */
  private FileOutputStream fos;

  /**
   * Constructor.
   * @param sopts static options
   */
  public Log(final StaticOptions sopts) {
    this.sopts = sopts;
  }

  /**
   * Writes a server entry to the log file.
   * @param type log type
   * @param info info string (can be {@code null})
   */
  public synchronized void writeServer(final LogType type, final String info) {
    write(SERVER, null, type, info, null);
  }

  /**
   * Writes an entry to the log file.
   * @param address address string
   * @param user user ({@code admin} if null)
   * @param type type (HTTP status code)
   * @param info info string (can be {@code null})
   * @param perf performance string
   */
  public synchronized void write(final String address, final User user, final int type,
      final String info, final Performance perf) {
    write(address, user, Integer.toString(type), info, perf);
  }

  /**
   * Writes an entry to the log file.
   * @param address address string
   * @param user user ({@code admin} if null)
   * @param type type (ERROR, OK, REQUEST, INFO)
   * @param info info string (can be {@code null})
   * @param perf performance string
   */
  public synchronized void write(final String address, final User user, final LogType type,
      final String info, final Performance perf) {
    write(address, user, type.toString(), info, perf);
  }

  /**
   * Writes an entry to the log file.
   * @param address address string
   * @param user user ({@code admin} if null)
   * @param type type (ERROR, OK, REQUEST, INFO, HTTP status code)
   * @param info info string (can be {@code null})
   * @param perf performance string
   */
  private synchronized void write(final String address, final User user, final String type,
      final String info, final Performance perf) {

    if(!sopts.get(StaticOptions.LOG)) {
      close();
      return;
    }

    // initializes the output stream and returns the current date
    final Date date = new Date();
    try {
      // check if day has changed
      final String nstart = DateTime.format(date, DateTime.DATE);
      if(fos != null && !start.equals(nstart)) close();

      // create new log file
      if(fos == null) {
        final IOFile dir = dir();
        dir.md();
        fos = new FileOutputStream(new IOFile(dir, nstart + IO.LOGSUFFIX).file(), true);
        start = nstart;
      }

      // construct log text
      final int ml = sopts.get(StaticOptions.LOGMSGMAXLEN);
      final TokenBuilder tb = new TokenBuilder();
      tb.add(DateTime.format(date, DateTime.TIME));
      tb.add('\t').add(address);
      tb.add('\t').add(user == null ? UserText.ADMIN : user.name());
      tb.add('\t').add(type);
      tb.add('\t').add(info == null ? EMPTY : chop(normalize(token(info)), ml));
      if(perf != null) tb.add('\t').add(perf.toString());
      tb.add(Prop.NL);

      // write and flush text
      fos.write(tb.finish());
      fos.flush();
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  /**
   * Closes the log file.
   */
  public synchronized void close() {
    if(fos == null) return;
    try {
      fos.close();
      fos = null;
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  /**
   * Returns a reference to the log directory.
   * @return log directory
   */
  public synchronized IOFile dir() {
    // log suffix, plural
    return sopts.dbpath(IO.LOGSUFFIX + 's');
  }

  /**
   * Returns all log files.
   * @return log directory
   */
  public synchronized IOFile[] files() {
    return dir().children(".*\\" + IO.LOGSUFFIX);
  }

  /**
   * Log entry.
   */
  public static class LogEntry {
    /** Time. */
    public String time;
    /** Address. */
    public String address;
    /** User. */
    public String user;
    /** Type. */
    public String type;
    /** Milliseconds. */
    public BigDecimal ms;
    /** Message. */
    public String message;
  }
}
