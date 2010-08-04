package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.io.IO;
import org.basex.util.Performance;

/**
 * Evaluates the 'restore' command and restores a backup of a database.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Restore extends Command {
  /**
   * Default constructor.
   * @param arg optional argument
   */
  public Restore(final String arg) {
    super(User.CREATE, arg);
  }

  @Override
  protected boolean run() {
    final Performance p = new Performance();
    String db = args[0];
    File file = null;
    final int i = db.indexOf("-");
    if(i == -1) {
      // find most recent backup
      for(final File f : new File(prop.get(Prop.DBPATH)).listFiles()) {
        final String n = f.getName();
        if(n.startsWith(db) && n.endsWith(IO.ZIPSUFFIX)) {
          if(file == null || file.lastModified() < f.lastModified()) file = f;
        }
      }
      if(file == null) return error("No Backup found for '" + db + "'");
    } else {
      if(!db.endsWith(IO.ZIPSUFFIX)) db += IO.ZIPSUFFIX;
      file = new File(prop.get(Prop.DBPATH) + Prop.SEP + db);
      db = db.substring(0, i);
    }
    if(!file.exists()) return error(FILEWHICH, db);

    // check if database is pinned
    if(context.pinned(db)) return error(DBLOCKED, db);
    // try to restore database
    return restore(file, prop) ? info(DBRESTORE, db, p) :
      error(DBNORESTORE, db);
  }

  /**
   * Restores the specified database.
   * @param file file
   * @param pr database properties
   * @return success flag
   */
  public static synchronized boolean restore(final File file, final Prop pr) {
    try {
      final InputStream is = new BufferedInputStream(new FileInputStream(file));
      final ZipInputStream zis = new ZipInputStream(is);
      final byte[] data = new byte[IO.BLOCKSIZE];
      ZipEntry e;
      while((e = zis.getNextEntry()) != null) {
        final String path = pr.get(Prop.DBPATH) + Prop.SEP + e.getName();
        if(e.isDirectory()) {
          new File(path).mkdir();
        } else {
          final BufferedOutputStream bos = new BufferedOutputStream(
              new FileOutputStream(path));
          int c;
          while((c = zis.read(data)) != -1) bos.write(data, 0, c);
          bos.close();
        }
      }
      zis.close();
      return true;
    } catch(final IOException io) {
      return false;
    }
  }
}
