package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.User;

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
    final String file = args[0];
    String db = "";
    try {
      db = file.substring(0, file.indexOf("-"));
    } catch(Exception e) {
      return error("Not a valid backup file.");
    }
    // DB is currently locked
    if(context.pinned(db)) return error(DBLOCKED, db);

    // try to alter database
    return restore(file, prop) ? info(DBRESTORE, db) : error(DBNORESTORE, db);
  }

  /**
   * Restores the specified database.
   * @param file file name
   * @param pr database properties
   * @return success flag
   */
  public static synchronized boolean restore(final String file, final Prop pr) {
    try {
      ZipFile zip = new ZipFile(new File(pr.get(Prop.DBPATH)
          + Prop.SEP + file));
      Enumeration<? extends ZipEntry> files = zip.entries(); 
      String destDir = "";
      byte[] buf = new byte[1024];
      while(files.hasMoreElements()) {
        ZipEntry entry = files.nextElement();
          if(entry.isDirectory()) {
            destDir = pr.get(Prop.DBPATH) + Prop.SEP + entry.getName();
            new File(destDir).mkdir();
          } else {
            File f = new File(pr.get(Prop.DBPATH) + Prop.SEP, entry.getName()); 
              InputStream in = zip.getInputStream(entry); 
              FileOutputStream out = new FileOutputStream(f);
              int len;
              while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
          } 
      }
      zip.close();
    } catch(IOException io) {
      return false;
    }
    return true;
  }
}
