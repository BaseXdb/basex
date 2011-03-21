package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.User;

/**
 * Evaluates the 'copy' command and creates a copy of a database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
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
    final File dest = new File(src.getParent() + '/' + newdb);
    dest.mkdir();
    String[] files = src.list();
    for (String file : files) {
      File srcFile = new File(src, file);
      File destFile = new File(dest, file);
      try {
      InputStream is = new FileInputStream(srcFile);
      OutputStream os = new FileOutputStream(destFile);
      byte[] buffer = new byte[1024];
      int length;
      while ((length = is.read(buffer)) > 0) {
        os.write(buffer, 0, length);
      }
      is.close();
      os.close();
      } catch (Exception ex) {
        return false;
      }
   }
    return true;
  }
}
