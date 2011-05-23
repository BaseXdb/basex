package org.basex.api.webdav;

import java.util.Date;

import org.basex.core.BaseXException;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.Open;

/**
 * WebDAV resource representing a database.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public abstract class BXDatabase extends BXResource {
  /** Database containing the document. */
  protected String dbname;

  @Override
  public Date getModifiedDate() {
    try {
      new Open(dbname).execute(ctx);
      final Date d = new Date(ctx.data.meta.time);
      new Close().execute(ctx);
      return d;
    } catch(BaseXException e) {
      // [DP] WebDAV: error handling
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public String getName() {
    return dbname;
  }
}
