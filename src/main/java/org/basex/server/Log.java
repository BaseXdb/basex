package org.basex.server;

import static org.basex.util.Token.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * Management of logging.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Andreas Weiler
 */
public final class Log {
  /** Date format. */
  private static final SimpleDateFormat DATE =
    new SimpleDateFormat("yyyy-MM-dd");
  /** Time format. */
  private static final SimpleDateFormat TIME =
    new SimpleDateFormat("HH:mm:ss.SSS");

  /** Quiet flag. */
  private final boolean quiet;
  /** Logging directory. */
  private final String dir;

  /** Start date of log. */
  private String start;
  /** Output stream. */
  private FileOutputStream fos;

  /**
   * Constructor.
   * @param ctx context reference
   * @param q quiet flag (no logging)
   */
  public Log(final Context ctx, final boolean q) {
    dir = ctx.prop.get(Prop.DBPATH) + "/.logs/";
    quiet = q;
    if(!q) create(new Date());
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
      tb.add(chop(token(s.toString().replaceAll("[\\r\\n ]+", " ")), 128));
    }
    tb.add(Prop.NL);

    // write text and flush log file
    try {
      fos.write(tb.finish());
      fos.flush();
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  /**
   * Creates a log file.
   * @param d date, used for file name
   */
  private synchronized void create(final Date d) {
    new File(dir).mkdirs();
    start = DATE.format(d);
    try {
      fos = new FileOutputStream(dir + start + ".log", true);
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
