package org.basex.server;

import static org.basex.util.Token.*;

import java.io.*;
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
  /** Quiet flag. */
  private final boolean quiet;
  /** Logging directory. */
  private final IOFile dir;
  /** Log message cut-off. */
  private final int maxlen;

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
    maxlen = ctx.mprop.num(MainProp.LOGMSGMAXLEN);
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
    if(!start.equals(DateTime.format(now, DateTime.DATE))) {
      close();
      create(now);
    }

    // construct log text
    final TokenBuilder tb = new TokenBuilder(DateTime.format(now, DateTime.TIME));
    for(final Object s : str) {
      tb.add('\t');
      tb.add(chop(token(s.toString().replaceAll("[\\r\\n ]+", " ")), maxlen));
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
    start = DateTime.format(d, DateTime.DATE);
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
