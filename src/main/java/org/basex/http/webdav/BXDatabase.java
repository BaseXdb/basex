package org.basex.http.webdav;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.http.*;

/**
 * WebDAV resource representing a collection database.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public final class BXDatabase extends BXFolder {
  /**
   * Constructor.
   * @param d database name
   * @param m last modified date
   * @param h http context
   */
  public BXDatabase(final String d, final long m, final HTTPContext h) {
    super(d, "", m, h);
  }

  @Override
  public String getName() {
    return db;
  }

  @Override
  protected void del() throws IOException {
    http.session().execute(new DropDB(db));
  }

  @Override
  protected void rename(final String n) throws IOException {
    http.session().execute(new AlterDB(db, n));
  }

  @Override
  protected void copyToRoot(final String n) throws IOException {
    http.session().execute(new Copy(db, n));
  }

  @Override
  protected void moveToRoot(final String n) throws IOException {
    rename(n);
  }
}
