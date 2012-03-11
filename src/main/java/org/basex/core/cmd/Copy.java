package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

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
    final String src = args[0];
    final String trg = args[1];
    // check if names are valid
    if(!MetaData.validName(src, false)) return error(NAME_INVALID_X, src);
    if(!MetaData.validName(trg, false)) return error(NAME_INVALID_X, trg);

    // source database does not exist
    if(!mprop.dbexists(src)) return error(DB_NOT_FOUND_X, src);
    // target database exists already
    if(mprop.dbexists(trg)) return error(DB_EXISTS_X, trg);

    // try to copy database
    return copy(src, trg) ? info(DB_COPIED_X, src, perf) : error(DB_NOT_COPIED_X, src);
  }

  @Override
  public String pinned(final Context ctx) {
    return null;
  }

  /**
   * Copies the specified database.
   * @param source name of the database
   * @param target new database name
   * @return success flag
   */
  private boolean copy(final String source, final String target) {
    final File src = mprop.dbpath(source).file();
    final File trg = mprop.dbpath(target).file();

    // return false if source cannot be opened, or target cannot be created
    final StringList files = new IOFile(src).descendants();
    tf = files.size();
    boolean ok = true;
    try {
      for(final String file : files) {
        if(Databases.FILES.matcher(file).matches()) {
          copy(new File(src, file), new File(trg, file));
        }
        of++;
      }
    } catch(final IOException ex) {
      Util.debug(ex);
      ok = false;
    }
    // drop new database if error occurred
    if(!ok) DropDB.drop(target, context);
    else context.databases().add(target);
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
    return COPY;
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
