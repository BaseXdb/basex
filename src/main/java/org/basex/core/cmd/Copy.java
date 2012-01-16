package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.basex.core.Command;
import org.basex.core.User;
import org.basex.data.MetaData;
import org.basex.io.IOFile;
import org.basex.util.Util;
import org.basex.util.list.StringList;

/**
 * Evaluates the 'copy' command and creates a copy of a database.
 *
 * @author BaseX Team 2005-12, BSD License
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
    if(!MetaData.validName(db, false)) return error(NAMEINVALID, db);
    if(!MetaData.validName(newdb, false)) return error(NAMEINVALID, newdb);

    // database does not exist
    if(!mprop.dbexists(db)) return error(DBNOTFOUND, db);
    // target database exists already
    if(mprop.dbexists(newdb)) return error(DBEXIST, newdb);

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
    final StringList files = new IOFile(src).descendants();
    tf = files.size();
    boolean ok = true;
    try {
      for(final String file : files) {
        copy(new File(src, file), new File(trg, file));
        of++;
      }
    } catch(final IOException ex) {
      Util.debug(ex);
      ok = false;
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
    final int bsize = (int) Math.max(1, Math.min(src.length(), 1 << 22));
    final byte[] buf = new byte[bsize];

    FileInputStream fis = null;
    FileOutputStream fos = null;
    try {
      // create parent directory of target file
      trg.getParentFile().mkdirs();
      fis = new FileInputStream(src);
      fos = new FileOutputStream(trg);
      // copy file buffer by buffer
      for(int i; (i = fis.read(buf)) != -1;) fos.write(buf, 0, i);
    } finally {
      // close file references
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
