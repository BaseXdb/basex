package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.basex.core.Command;
import org.basex.core.User;

/**
 * Evaluates the 'copy' command and creates a copy of a database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Andreas Weiler
 */
public final class Copy extends Command {
  /** Counter for outstanding files. */
  private int of;
  /** Counter of total files. */
  private int tf;

  /**
   * Default constructor.
   * @param db db name
   * @param newdb new db name
   */
  public Copy(final String db, final String newdb) {
    super(User.CREATE, db, newdb);
  }

  @Override
  protected boolean run() {
    final String db = args[0];
    final String newdb = args[1];
    // check if names are valid
    if(!validName(db, false)) return error(NAMEINVALID, db);
    if(!validName(newdb, false)) return error(NAMEINVALID, newdb);

    // database does not exist
    if(!mprop.dbexists(db)) return error(DBNOTFOUND, db);
    // target database exists already
    if(mprop.dbexists(newdb)) return error(DBEXISTS, newdb);

    // try to copy database
    return copy(db, newdb) ? info(DBCOPY, db, perf) : error(DBNOCOPY, db);
  }

  /**
   * Copies the specified database.
   * @param db database name
   * @param newdb new database name
   * @return success flag
   */
  private boolean copy(final String db, final String newdb) {
    final File src = mprop.dbpath(db);
    final File trg = mprop.dbpath(newdb);

    // return false if source cannot be opened, or target cannot be created
    final String[] files = src.list();
    if(files == null || !trg.mkdir()) return false;
    tf = files.length;
    boolean ok = true;
    for(final String file : files) {
      of++;
      try {
        copy(new File(src, file), new File(trg, file));
      } catch(final IOException ex) {
        ok = false;
        break;
      }
    }
    // drop new database if error occurred
    if(!ok) DropDB.drop(newdb, mprop);
    return ok;
  }

  /**
   * Copies the specified file.
   * @param src source file
   * @param trg target file
   * @throws IOException I/O exception
   */
  public static synchronized void copy(final File src, final File trg)
      throws IOException {

    // optimize buffer size
    final byte[] buf = new byte[(int) Math.min(src.length(), 1 << 22)];
    FileInputStream fis = null;
    FileOutputStream fos = null;
    try {
      fis = new FileInputStream(src);
      fos = new FileOutputStream(trg);
      for(int i; (i = fis.read(buf)) != -1;) fos.write(buf, 0, i);
    } finally {
      if(fis != null) try { fis.close(); } catch(final IOException ex) { }
      if(fos != null) try { fos.close(); } catch(final IOException ex) { }
    }
  }

  @Override
  protected String tit() {
    return BUTTONCOPY;
  }

  @Override
  public boolean supportsProg() {
    return true;
  }

  @Override
  protected double prog() {
    return (double) of / tf;
  }
}
