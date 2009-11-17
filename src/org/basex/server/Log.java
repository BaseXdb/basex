package org.basex.server;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.util.Token;

/**
 * Management of logging.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class Log {
  /** Context. */
  private Context ctx;
  /** Log files folder. */
  private IO logdir;
  /** Daily log file. */
  private IO logfile;
  /** Date. */
  private Date d = new Date();

  /**
   * Constructor.
   * @param c Context
   */
  public Log(final Context c) {
    ctx = c;
    logdir = IO.get(ctx.prop.get(Prop.DBPATH) + "\\Logs");
    if(!logdir.exists()) {
      new File(ctx.prop.get(Prop.DBPATH) + "\\Logs").mkdir();
    }
    DateFormat df = DateFormat.getDateInstance(2);
    logfile = IO.get(ctx.prop.get(Prop.DBPATH) + "\\Logs\\" + df.format(d)
        + ".txt");
    if(!logfile.exists()) {
      try {
        new File(ctx.prop.get(Prop.DBPATH) + "\\Logs\\" +
            df.format(d) + ".txt").createNewFile();
        logfile = IO.get(ctx.prop.get(Prop.DBPATH) + "\\Logs\\" + df.format(d)
            + ".txt");
      } catch(IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Writes into the logfile.
   * @param s String
   */
  public void write(final String s) {
    try {
      DateFormat df = DateFormat.getTimeInstance(2);
      String tmp = df.format(d) + " : " + s;
      logfile.write(Token.token(tmp));
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

}
