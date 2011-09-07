package org.basex.api.webdav;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.basex.core.BaseXException;

import org.basex.core.Text;
import org.basex.core.cmd.AlterDB;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.Copy;
import org.basex.core.cmd.DropDB;
import org.basex.server.Query;
import org.basex.server.Session;

/**
 * WebDAV resource representing a collection database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXDatabase extends BXFolder {
  /** Date format of the "Time Stamp" field in INFO DB. */
  private static final DateFormat DATEFORMAT = new SimpleDateFormat(
      "dd.MM.yyyy HH:mm:ss");

  /**
   * Constructor.
   * @param dbname database name
   * @param f resource factory
   * @param u user name
   * @param p user password
   */
  public BXDatabase(final String dbname, final BXResourceFactory f,
      final String u, final String p) {
    super(dbname, "", f, u, p);
  }

  @Override
  public Date getModifiedDate() {
    try {
      final String info;
      final Session s = factory.login(user, pass);
      try {
        final Query q = s.query("db:info($p)");
        q.bind("$p", db);
        info = q.execute();
      } finally {
        s.close();
      }
      // parse the timestamp
      final String timestamp = "Time Stamp: ";
      final int p = info.indexOf(timestamp);
      if(p >= 0) {
        final String date = info.substring(p + timestamp.length(),
            info.indexOf(Text.NL, p));
        if(date.length() > 0) return DATEFORMAT.parse(date);
      }
    } catch(final Exception ex) {
      handle(ex);
    }
    return null;
  }

  @Override
  public String getName() {
    return db;
  }

  @Override
  protected void delete(final Session s) throws BaseXException {
    s.execute(new Close());
    s.execute(new DropDB(db));
  }

  @Override
  protected void rename(final Session s, final String n) throws BaseXException {
    s.execute(new AlterDB(db, n));
  }

  @Override
  protected void copyToRoot(final Session s, final String n)
      throws BaseXException {
    s.execute(new Copy(db, n));
  }

  @Override
  protected void moveToRoot(final Session s, final String n)
      throws BaseXException {
    rename(s, n);
  }
}
