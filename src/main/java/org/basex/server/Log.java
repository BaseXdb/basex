package org.basex.server;

import static org.basex.util.Token.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.io.IOFile;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

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
  private synchronized void create(final Date d) {
    dir.md();
    start = DATE.format(d);
    try {
      fos = new FileOutputStream(new IOFile(dir, start + ".log").file(), true);
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  /**
   * Closes the log file.
   */
  public synchronized void close() {
    if(quiet) return;
    try {
      fos.close();
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }
}
