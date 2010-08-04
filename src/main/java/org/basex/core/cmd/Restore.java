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

/**
 * Evaluates the 'restore' command and restores a backup of a database.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Restore extends Command {

  /** Buffer size. */
  private static final int SIZE = 1024;

  /**
   * Default constructor.
   * @param arg optional argument
   */
  public Restore(final String arg) {
    super(User.CREATE, arg);
  }

  @Override
  protected boolean run() {
    String filename = args[0];
    File file = null;
    String db = "";
    int i = filename.indexOf("-");
    if(i == -1) {
      File folder = new File(prop.get(Prop.DBPATH));
      String[] files = folder.list();
      File newest = null;
      int c = 0;
      for(String n : files) {
        if(n.startsWith(filename) && n.endsWith(".zip")) {
          db = filename;
          if(c == 0) newest = new File(prop.get(Prop.DBPATH) + Prop.SEP + n);
          File tmp = new File(prop.get(Prop.DBPATH) + Prop.SEP + n);
          if(tmp.lastModified() < newest.lastModified()) newest = tmp;
          c++;
        }
      }
      file = newest;
      if(file == null) return error("No Backup found for '" + filename + "'");
    } else {
      db = filename.substring(0, i);
      if(!filename.endsWith(".zip")) filename = filename.concat(".zip");
      file = new File(prop.get(Prop.DBPATH) + Prop.SEP + filename);
    }
    if(!file.exists()) return error(FILEWHICH, filename);
    // check if database is pinned
    if(context.pinned(db)) return error(DBLOCKED, db);
    // try to restore database
    return restore(file, prop) ? info(DBRESTORE, db) : error(DBNORESTORE, db);
  }

  /**
   * Restores the specified database.
   * @param file file
   * @param pr database properties
   * @return success flag
   */
  public static synchronized boolean restore(final File file, final Prop pr) {
    try {
      InputStream is = new BufferedInputStream(new FileInputStream(file));
      ZipInputStream zis = new ZipInputStream(is);
      ZipEntry e;
      while((e = zis.getNextEntry()) != null) {
        if(e.isDirectory()) {
          new File(pr.get(Prop.DBPATH) + Prop.SEP + e.getName()).mkdir();
        } else {
          BufferedOutputStream bos = new BufferedOutputStream(new
              FileOutputStream(pr.get(Prop.DBPATH) + Prop.SEP + e.getName()));
          byte[] data = new byte[SIZE];
          int count;
          while((count = zis.read(data, 0, SIZE)) != -1) {
            bos.write(data, 0, count);
          }
          bos.close();
        }
      }
      zis.close();
    } catch(IOException io) {
      return false;
    }
    return true;
  }
}
