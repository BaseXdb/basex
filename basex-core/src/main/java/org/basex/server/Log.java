package org.basex.server;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * This class writes daily log files to disk.
 * The log format has been updated in Version 7.4; it now has the following columns:
 * <ul>
 *   <li><b>Time</b>: timestamp (format: {@code xs:time})</li>
 *   <li><b>Address</b>: host name and port of the requesting client</li>
 *   <li><b>User</b>: user name</li>
 *   <li><b>Type</b>: Type of logging message: REQUEST, OK or ERROR</li>
 *   <li><b>Info</b>: Logging message</li>
 *   <li><b>Performance</b>: Measured time in milliseconds</li>
 * </ul>
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Log {
  /** SERVER string. */
  public static final String SERVER = "SERVER";
  /** ERROR string. */
  private static final String ERROR = "ERROR";
  /** REQUEST string. */
  private static final String REQUEST = "REQUEST";

  /** Global options. */
  private final GlobalOptions gopts;
  /** Start date of log. */
  private String start;
  /** Output stream. */
  private FileOutputStream fos;

  /**
   * Constructor.
   * @param ctx database context
   */
  public Log(final Context ctx) {
    gopts = ctx.globalopts;
  }

  /**
   * Writes an error to the log file.
   * @param th throwable
   */
  public synchronized void writeError(final Throwable th) {
    writeServer(ERROR, Util.message(th));
  }

  /**
   * Writes a server entry to the log file.
   * @param str strings to be written
   */
  public synchronized void writeServer(final Object... str) {
    final Object[] tmp = new Object[str.length + 2];
    tmp[0] = SERVER;
    tmp[1] = S_ADMIN;
    System.arraycopy(str, 0, tmp, 2, str.length);
    write(tmp);
  }

  /**
   * Writes an entry to the log file.
   * @param str strings to be written
   */
  public synchronized void write(final Object... str) {
    if(!gopts.get(GlobalOptions.LOG)) {
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
      final int ml = gopts.get(GlobalOptions.LOGMSGMAXLEN);
      final TokenBuilder tb = new TokenBuilder(DateTime.format(date, DateTime.TIME));
      for(final Object s : str) {
        tb.add('\t');
        final String st;
        if(s == null) st = REQUEST;
        else if(s instanceof Boolean) st = (Boolean) s ? OK : ERROR;
        else if(s instanceof Throwable) st = Util.message((Throwable) s);
        else st = s.toString();
        tb.add(chop(token(st.replaceAll("\\s+", " ").trim()), ml));
      }
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
    return gopts.dbpath(IO.LOGSUFFIX + 's');
  }

  /**
   * Returns all log files.
   * @return log directory
   */
  public synchronized IOFile[] files() {
    return dir().children(".*\\" + IO.LOGSUFFIX);
  }
}
