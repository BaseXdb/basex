package org.basex.server;

import static org.basex.util.Token.*;

import java.io.*;
import java.text.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * This class writes logging information to disk.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Andreas Weiler
 */
public final class Log {
  /** Date format. */
  private static final DateFormat DATE = new SimpleDateFormat("yyyy-MM-dd");
  /** Time format. */
  private static final DateFormat TIME = new SimpleDateFormat("HH:mm:ss.SSS");

  /** Quiet flag. */
  private final boolean quiet;
  /** Logging directory. */
  private final IOFile dir;

  /** Start date of log. */
  private String start;
  /** Output stream. */
  private FileOutputStream fos;

  /**
   * Constructor.
   * @param ctx database context
   * @param q quiet flag (no logging)
   */
  public Log(final Context ctx, final boolean q) {
    dir = ctx.mprop.dbpath(".logs");
    quiet = q;
    if(!q) create(new Date());
  }

  /**
   * Writes an error to the log file.
   * @param th throwable
   */
  public synchronized void error(final Throwable th) {
    Util.stack(th);
    if(!quiet) write(Util.bug(th));
  }

  /**
   * Writes an entry to the log file.
   * @param str strings to be written
   */
  public synchronized void write(final Object... str) {
    if(quiet) return;

    // check if current log file is still up-to-date
    final Date now = new Date();
    if(!start.equals(DATE.format(now))) {
      close();
      create(now);
    }

    // construct log text
    final TokenBuilder tb = new TokenBuilder(TIME.format(now));
    for(final Object s : str) {
      tb.add('\t');
      tb.add(chop(token(s.toString().replaceAll("[\\r\\n ]+", " ")), 1000));
    }
    tb.add(Prop.NL);

    // write text and flush log file
    try {
      fos.write(tb.finish());
      fos.flush();
    } catch(final Exception ex) {
      Util.debug(ex);
    }
  }

  /**
   * Creates a log file.
   * @param d date, used for file name
   */
  private void create(final Date d) {
    dir.md();
    synchronized(DATE) { start = DATE.format(d); }
    try {
      fos = new FileOutputStream(new IOFile(dir, start + ".log").file(), true);
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  /**
   * Closes the log file.
   */
  private void close() {
    try {
      fos.close();
      fos = null;
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }
}
