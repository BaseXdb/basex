package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.User;

/**
 * Evaluates the 'copy' command and creates a copy of a database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Andreas Weiler
 */
public class Copy extends Command {
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
    if(!validName(db)) return error(NAMEINVALID, db);
    if(!validName(newdb)) return error(NAMEINVALID, newdb);

    // check if new database already exists
    if(prop.dbexists(newdb)) return error(DBEXISTS, newdb);

    // try to copy database
    return !prop.dbexists(db) ? error(DBNOTFOUND, db) :
      copy(db, newdb, prop) ? info(DBCOPY, db, perf) : error(DBNOCOPY, db);
  }

  /**
   * Copies the specified database.
   * @param db database name
   * @param newdb new database name
   * @param pr database properties
   * @return success flag
   */
  private boolean copy(final String db, final String newdb, final Prop pr) {
    final File src = pr.dbpath(db);
    final File trg = pr.dbpath(newdb);

    // return false if source cannot be opened, or target cannot be created
    final String[] files = src.list();
    if(files == null || !trg.mkdir()) return false;

    boolean ok = true;
    for(final String file : files) {
      FileChannel sc = null;
      FileChannel dc = null;
      try {
        sc = new FileInputStream(new File(src, file)).getChannel();
        dc = new FileOutputStream(new File(trg, file)).getChannel();
        dc.transferFrom(sc, 0, sc.size());
      } catch(final IOException ex) {
        ok = false;
      } finally {
        if(sc != null) try { sc.close(); } catch(final IOException ex) { }
        if(dc != null) try { dc.close(); } catch(final IOException ex) { }
      }
    }
    // drop new database if error occurred
    if(!ok) DropDB.drop(newdb, pr);
    return ok;
  }
}
