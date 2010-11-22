package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import org.basex.io.IO;

/**
 * Evaluates the 'backup' command and creates a backup of a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Backup extends Command {
  /** Date format. */
  private static final SimpleDateFormat DATE = new SimpleDateFormat(
      "yyyy-MM-dd-HH-mm-ss");

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
    if(!checkName(db)) return error(NAMEINVALID, db);

    // try to backup database
    return !prop.dbexists(db) ? error(DBNOTFOUND, db) :
      backup(db, prop) ? info(DBBACKUP, db, perf) : error(DBNOBACKUP, db);
  }

  /**
   * Backups the specified database.
   * @param db database name
   * @param pr database properties
   * @return success flag
   */
  public static boolean backup(final String db, final Prop pr) {
    try {
      final File in = pr.dbpath(db);
      final File out = new File(pr.get(Prop.DBPATH) + Prop.SEP + db + "-" +
          DATE.format(new Date()) + IO.ZIPSUFFIX);
      final byte[] data = new byte[IO.BLOCKSIZE];

      // OutputStream for zipping
      final ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(
          new FileOutputStream(out)));
      // Process each file
      for(final File f : in.listFiles()) {
        final BufferedInputStream bis = new BufferedInputStream(
            new FileInputStream(f), IO.BLOCKSIZE);
        zos.putNextEntry(new ZipEntry(in.getName() + '/' + f.getName()));
        int c;
        while((c = bis.read(data)) != -1) zos.write(data, 0, c);
        zos.closeEntry();
        bis.close();
      }
      zos.close();
      return true;
    } catch(final IOException e) {
      return false;
    }
  }
}
