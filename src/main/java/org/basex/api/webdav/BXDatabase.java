package org.basex.api.webdav;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.basex.core.Text;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.Open;
import org.basex.server.ClientSession;

/**
 * WebDAV resource representing a database.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public abstract class BXDatabase extends BXResource {
  /** Date format of the "Time Stamp" field in INFO DB. */
  private static final DateFormat DATEFORMAT = new SimpleDateFormat(
      "dd.MM.yyyy HH:mm:ss");
  /** Database containing the document. */
  protected String dbname;

  @Override
  public Date getModifiedDate() {
    try {
      final ClientSession cs = login(user, pass);
      try {
        cs.execute(new Open(dbname));
        final String info = cs.query("db:info()").execute();
        cs.execute(new Close());

        // parse the timestamp
        final String timestamp = "Time Stamp: ";
        final int p = info.indexOf(timestamp);
        if(p < 0) return null;
        final String date = info.substring(p + timestamp.length(),
            info.indexOf(Text.NL, p));
        return DATEFORMAT.parse(date);
      } finally {
        cs.close();
      }
    } catch(Exception e) {
      e.printStackTrace();
      // [DP] WebDAV: error handling
    }
    return null;
  }

  @Override
  public String getName() {
    return dbname;
  }
}
