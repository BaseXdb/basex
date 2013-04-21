package org.basex.http.webdav.milton2;

import org.basex.http.webdav.impl.ResourceMetaData;
import org.basex.http.webdav.impl.WebDAVService;

import java.io.IOException;

/**
 * WebDAV resource representing a collection database.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 */
public class BXDatabase extends BXFolder {
  /**
   * Constructor.
   * @param d resource meta data
   * @param s service implementation
   */
  public BXDatabase(final ResourceMetaData d, final WebDAVService<BXAbstractResource> s) {
    super(d, s);
  }

  @Override
  public String getName() {
    return meta.db;
  }

  @Override
  protected void del() throws IOException {
    service.dropDb(meta.db);
  }

  @Override
  protected void rename(final String n) throws IOException {
    service.renameDb(meta.db, n);
  }

  @Override
  protected void copyToRoot(final String n) throws IOException {
    service.copyDb(meta.db, n);
  }

  @Override
  protected void moveToRoot(final String n) throws IOException {
    rename(n);
  }
}
