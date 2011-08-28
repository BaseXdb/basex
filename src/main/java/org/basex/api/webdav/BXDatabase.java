package org.basex.api.webdav;

import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.basex.core.Text;
import org.basex.core.cmd.DropDB;
import org.basex.server.Query;
import org.basex.server.Session;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Range;

/**
 * WebDAV resource representing a collection database.
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
        final Query q = s.query("declare variable $p as xs:string external; "
            + "db:info($p)");
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
    } catch(Exception ex) {
      handle(ex);
    }
    return null;
  }

  @Override
  public String getName() {
    return db;
  }

  @Override
  public void copyTo(final CollectionResource toCollection, final String name) {
  }

  @Override
  public void delete() {
    try {
      final Session s = factory.login(user, pass);
      try {
        s.execute(new DropDB(db));
      } finally {
        s.close();
      }
    } catch(Exception ex) {
      handle(ex);
    }

  }

  @Override
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType) {
    // may not be needed to be implemented
  }

  @Override
  public String getContentType(final String accepts) {
    return null;
  }

  @Override
  public void moveTo(final CollectionResource rDest, final String name) {
  }
}
