package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.User;

/**
 * Evaluates the 'backup' command and creates a backup of a database.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Backup extends Command {

  /** Date format. */
  private static final SimpleDateFormat DATE = new SimpleDateFormat(
      "yyyy-MM-dd");
  /** Time format. */
  private static final SimpleDateFormat TIME = new SimpleDateFormat("HH-mm-ss");
  /** Buffer size. */
  private static final int SIZE = 1024;

  /**
   * Default constructor.
   * @param arg optional argument
   */
  public Backup(final String arg) {
    super(User.CREATE, arg);
  }

  @Override
  protected boolean run() {
    final String db = args[0];
    // try to backup database
    return !prop.dbexists(db) ? error(DBNOTFOUND, db)
        : backup(db, prop) ? info(DBBACKUP, db) : error(DBNOBACKUP, db);
  }

  /**
   * Backups the specified database.
   * @param db database name
   * @param pr database properties
   * @return success flag
   */
  public static synchronized boolean backup(final String db, final Prop pr) {
    File inFolder = pr.dbpath(db);
    final Date now = new Date();
    
    try {
      File outFile = new File(pr.get(Prop.DBPATH) + Prop.SEP + db + "-"
          + DATE.format(now) + "-" + TIME.format(now) + ".zip");    
      // OutputStream for zipping
      ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
          new FileOutputStream(outFile)));
      byte[] data = new byte[SIZE];
      // List of files in the folder
      String[] files = inFolder.list();
      // Create folder in the zip
      out.putNextEntry(new ZipEntry(inFolder.getName() + "/"));
      out.closeEntry();
      // Process each file
      for(String s : files) {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(
            inFolder.getPath() + Prop.SEP + s), SIZE);
        out.putNextEntry(new ZipEntry(inFolder.getName() + "/" + s));
        int count;
        while((count = in.read(data, 0, SIZE)) != -1) {
          out.write(data, 0, count);
        }
        out.closeEntry();
        in.close();
      }
      out.flush();
      out.close();
      return true;
    } catch(Exception e) {
      return false;
    }
  }
}
