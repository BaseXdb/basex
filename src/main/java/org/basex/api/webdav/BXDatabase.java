package org.basex.api.webdav;

import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.basex.core.Text;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Open;
import org.basex.server.ClientQuery;
import org.basex.server.ClientSession;
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
   */
  public BXDatabase(final String dbname) {
    super(dbname, "");
  }

  /**
   * Constructor.
   * @param dbname database name
   * @param u user name
   * @param p user password
   */
  public BXDatabase(final String dbname, final String u,
      final String p) {
    super(dbname, "", u, p);
  }

  @Override
  public Date getModifiedDate() {
    try {
      final String info;
      final ClientSession cs = login(user, pass);
      try {
        //cs.execute(new Open(db));
        //info = cs.query("db:info()").execute();
        ClientQuery q = cs.query("declare variable $p as xs:string external; "
            + "db:info($p)");
        q.bind("$p", db);
        info = q.execute();
      } finally {
        cs.close();
      }
      // parse the timestamp
      final String timestamp = "Time Stamp: ";
      final int p = info.indexOf(timestamp);
      if(p >= 0) {
        final String date = info.substring(p + timestamp.length(),
            info.indexOf(Text.NL, p));
        if(date.length() > 0) return DATEFORMAT.parse(date);
      }
    } catch(Exception e) {
      e.printStackTrace();
      // [DP] WebDAV: error handling
    }
    return null;
  }

  @Override
  public String getName() {
    return db;
  }

  @Override
  public void copyTo(final CollectionResource toCollection, final String name) {
    // TODO Auto-generated method stub
  }

  @Override
  public void delete() {
    try {
      final ClientSession cs = login(user, pass);
      try {
        cs.execute(new DropDB(db));
      } finally {
        cs.close();
      }
    } catch(Exception e) {
      // [RS] WebDAV: error handling
      e.printStackTrace();
    }

  }

  @Override
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType) {
    // TODO Auto-generated method stub
  }

  @Override
  public String getContentType(final String accepts) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void moveTo(final CollectionResource rDest, final String name) {
    // TODO Auto-generated method stub
  }
}
