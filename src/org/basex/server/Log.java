package org.basex.server;

import static org.basex.core.Text.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.basex.core.Prop;
import org.basex.io.IO;

/**
 * Management of logging.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class Log {
  /** Log folder checker. */
  private IO logdir;
  /** Daily log file. */
  private File logfile;
  /** Start date of log. */
  private Date start;
  /** FileWriter. */
  private FileWriter fw;

  /**
   * Constructor.
   */
  public Log() {
    start = new Date();
    createLogDir();
    createLogFile(start);
  }

  /**
   * Creates a folder for log files.
   */
  private void createLogDir() {
    logdir = IO.get(Prop.HOME + "/BaseXLogs");
    if(!logdir.exists()) {
      new File(Prop.HOME + "/BaseXLogs").mkdir();
    }
  }

  /**
   * Creates a log file.
   * @param d Date
   */
  private void createLogFile(final Date d) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    logfile = new File(Prop.HOME + "/BaseXLogs/" +
        sdf.format(d) + ".log");
    try {
      fw = new FileWriter(logfile, true);
    } catch(IOException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Checks if a new day is on.
   * @param d Date
   * @return boolean for new day
   */
  private boolean checkDay(final Date d) {
    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
    String past = sdf.format(start);
    String present = sdf.format(d);
    return past.equals(present);
  }

  /**
   * Writes into the log file.
   * @param s String
   */
  public void write(final String s) {
    Date now = new Date();
    if (!checkDay(now)) {
      try {
        fw.close();
      } catch(IOException e) {
        e.printStackTrace();
      }
      createLogFile(now);
    }
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    String tmp = sdf.format(now) + " : " + s + NL;
    try {
      fw.write(tmp);
    } catch(IOException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Closes the log file.
   */
  public void closeLog() {
    try {
      fw.close();
    } catch(IOException e) {
      e.printStackTrace();
    }
  }
}
