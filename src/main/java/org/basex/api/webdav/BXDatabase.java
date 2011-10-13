package org.basex.api.webdav;

import java.io.IOException;

import org.basex.api.HTTPSession;
import org.basex.core.cmd.AlterDB;
import org.basex.core.cmd.Copy;
import org.basex.core.cmd.DropDB;
import org.basex.server.Session;

/**
 * WebDAV resource representing a collection database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public final class BXDatabase extends BXFolder {
  /**
   * Constructor.
   * @param d database name
   * @param m last modified date
   * @param s current session
   */
  public BXDatabase(final String d, final long m, final HTTPSession s) {
    super(d, "", m, s);
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
