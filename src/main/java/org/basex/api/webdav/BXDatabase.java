package org.basex.api.webdav;

import static org.basex.query.func.Function.*;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.basex.api.HTTPSession;
import org.basex.core.Text;
import org.basex.core.cmd.AlterDB;
import org.basex.core.cmd.Copy;
import org.basex.core.cmd.DropDB;
import org.basex.server.Query;
import org.basex.server.Session;
import org.basex.util.Util;

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
   * @param s current session
   */
  public BXDatabase(final String dbname, final HTTPSession s) {
    super(dbname, "", s);
  }

  @Override
  public Date getModifiedDate() {
    try {
      final String info = new BXCode<String>(this) {
        @Override
        public String get() throws IOException {
          final Query q = s.query(DBINFO.args("$p"));
          q.bind("p", db);
          return q.execute();
        }
      }.eval();

      // parse the timestamp
      final String timestamp = Text.INFOTIME + Text.COLS;
      final int p = info.indexOf(timestamp);
      if(p >= 0) {
        final String date = info.substring(p + timestamp.length(),
            info.indexOf(Text.NL, p));
        if(date.length() > 0) return DATEFORMAT.parse(date);
      }
    } catch(final Exception ex) {
      Util.errln(ex);
    }
    return null;
  }

  @Override
  public String getName() {
    return db;
  }

  @Override
  protected void delete(final Session s) throws IOException {
    s.execute(new DropDB(db));
  }

  @Override
  protected void rename(final Session s, final String n) throws IOException {
    s.execute(new AlterDB(db, n));
  }

  @Override
  protected void copyToRoot(final Session s, final String n)
      throws IOException {
    s.execute(new Copy(db, n));
  }

  @Override
  protected void moveToRoot(final Session s, final String n)
      throws IOException {
    rename(s, n);
  }
}
