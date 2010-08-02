package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.User;

/**
 * Evaluates the 'backup' command creates a backup of a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Backup extends Command {
  
  /** Date format. */
  private static final SimpleDateFormat DATE =
    new SimpleDateFormat("yyyy-MM-dd");
  /** Time format. */
  private static final SimpleDateFormat TIME =
    new SimpleDateFormat("HH-mm-ss");
  
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
    // try to alter database
    return !prop.dbexists(db) ? error(DBNOTFOUND, db) :
      backup(db, prop) ? info(DBBACKUP, db) :
        error(DBNOBACKUP, db);
  }
  
  /**
   * Backups the specified database.
   * @param db database name
   * @param pr database properties
   * @return success flag
   */
  public static synchronized boolean backup(final String db, final Prop pr) {
    String[] filenames = pr.dbpath(db).list();
    final Date now = new Date();
    byte[] buf = new byte[1024];
    try {
        File zip = new File(pr.get(Prop.DBPATH) + Prop.SEP + db
            + "-" + DATE.format(now) + "-" + TIME.format(now) + ".zip");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
        for (String file : filenames) {
            FileInputStream in = new FileInputStream(pr.dbpath(db)
                + Prop.SEP + file);
            out.putNextEntry(new ZipEntry(file));
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
            in.close();
        }
        out.close();
        return true;
    } catch (IOException e) {
      return false;
    }
  }
}
