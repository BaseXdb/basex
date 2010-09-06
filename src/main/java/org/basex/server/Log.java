package org.basex.server;

import static org.basex.core.Text.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Management of logging.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
  private Date start;
  /** File writer. */
  private OutputStreamWriter fw;

  /**
   * Constructor.
   * @param ctx context reference
   * @param q quiet flag (no logging)
   */
  public Log(final Context ctx, final boolean q) {
    dir = ctx.prop.get(Prop.DBPATH) + "/.logs/";
    quiet = q;
    if(quiet) return;

    create(new Date());
    write(SERVERSTART);

    // guarantee correct shutdown...
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        write(SERVERSTOPPED);
        close();
      }
    });
  }

  /**
   * Writes into the log file.
   * @param str strings to be written
   */
  public synchronized void write(final Object... str) {
    if(quiet) return;

    final Date now = new Date();
    if(!DATE.format(start).equals(DATE.format(now))) {
      close();
      create(now);
    }
    try {
      final StringBuilder sb = new StringBuilder(TIME.format(now));
      for(final Object s : str) {
        sb.append("\t" + s.toString().replaceAll("\\r?\\n", " "));
      }
      fw.write(sb.append(NL).toString());
      fw.flush();
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  /**
   * Creates a log file.
   * @param d Date
   */
  private synchronized void create(final Date d) {
    new File(dir).mkdirs();
    final String file = dir + DATE.format(d) + ".log";
    start = d;
    try {
      fw = new OutputStreamWriter(
          new FileOutputStream(file, true), Token.UTF8);
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  /**
   * Closes the log file.
   */
  synchronized void close() {
    try {
      fw.close();
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }
}
