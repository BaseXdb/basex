package org.basex.server;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

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
  /** Log files folder. */
  private IO logdir;
  /** Daily log file. */
  private IO logfile;
  /** Date. */
  private Date d = new Date();

  /**
   * Constructor.
   */
  public Log() {
    logdir = IO.get(Prop.HOME + "\\BaseXLogs");
    if(!logdir.exists()) {
      new File(Prop.HOME + "\\BaseXLogs").mkdir();
    }
    DateFormat df = DateFormat.getDateInstance(2);
    logfile = IO.get(Prop.HOME + "\\BaseXLogs\\" + df.format(d)
        + ".log");
    if(!logfile.exists()) {
      try {
        new File(Prop.HOME + "\\BaseXLogs\\" +
            df.format(d) + ".log").createNewFile();
        logfile = IO.get(Prop.HOME + "\\BaseXLogs\\" + df.format(d)
            + ".log");
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
