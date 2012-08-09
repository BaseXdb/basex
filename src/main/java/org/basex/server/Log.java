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
 * @author Christian Gruen
 */
public final class Log {
  /** Main properties. */
  private final MainProp mprop;
  /** Start date of log. */
  private String start;
  /** Output stream. */
  private FileOutputStream fos;

  /**
   * Constructor.
   * @param ctx database context
   */
  public Log(final Context ctx) {
    mprop = ctx.mprop;
  }

  /**
   * Writes an error to the log file.
   * @param th throwable
   */
  public synchronized void error(final Throwable th) {
    Util.stack(th);
    write(Util.bug(th));
  }

  /**
   * Writes an entry to the log file.
   * @param str strings to be written
   */
  public synchronized void write(final Object... str) {
    if(!mprop.is(MainProp.LOG)) return;

    // initializes the output stream and returns the current date
    final Date date = new Date();
    try {
      // day has changed..
      if(fos != null && !start.equals(DateTime.format(date, DateTime.DATE))) {
        fos.close();
        fos = null;
      }
      // create new log file
      if(fos == null) {
        final IOFile dir = mprop.dbpath(".logs");
        dir.md();
        start = DateTime.format(date, DateTime.DATE);
        fos = new FileOutputStream(new IOFile(dir, start + ".log").file(), true);
      }

      // construct log text
      final int ml = mprop.num(MainProp.LOGMSGMAXLEN);
      final TokenBuilder tb = new TokenBuilder(DateTime.format(date, DateTime.TIME));
      for(final Object s : str) {
        tb.add('\t').add(chop(token(s.toString().replaceAll("[\\r\\n ]+", " ")), ml));
      }
      tb.add(Prop.NL);

      fos.write(tb.finish());
      fos.flush();
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }
}
