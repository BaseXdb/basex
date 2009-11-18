package org.basex.server;

import static org.basex.core.Text.*;
import java.io.BufferedWriter;
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
  /** Date. */
  private Date d = new Date();
  /** BufferedWriter. */
  private BufferedWriter bw;

  /**
   * Constructor.
   */
  public Log() {
    logdir = IO.get(Prop.HOME + "\\BaseXLogs");
    if(!logdir.exists()) {
      new File(Prop.HOME + "\\BaseXLogs").mkdir();
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    logfile = new File(Prop.HOME + "\\BaseXLogs\\" +
        sdf.format(d) + ".log");
    if(!logfile.exists()) {
        try {
          logfile.createNewFile();
        } catch(IOException e) {
          e.printStackTrace();
        }
    }
    try {
      bw = new BufferedWriter(new FileWriter(logfile, true));
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Writes into the logfile.
   * @param s String
   */
  public void write(final String s) {
    try {
      d = new Date();
      SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
      String tmp = sdf.format(d) + " : " + s + NL;
      bw.write(tmp); 
    } catch(IOException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Closes the log.
   */
  public void closeLog() {
    try {
      bw.close();
    } catch(IOException e) {
      e.printStackTrace();
    }
  }
}
