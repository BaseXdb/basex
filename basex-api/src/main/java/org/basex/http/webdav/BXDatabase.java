package org.basex.http.webdav;

import java.io.*;

import org.basex.http.webdav.impl.*;

/**
 * WebDAV resource representing a collection database.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
final class BXDatabase extends BXFolder {
  /**
   * Constructor.
   * @param meta resource meta data
   * @param service service
   */
  BXDatabase(final ResourceMetaData meta, final WebDAVService<BXAbstractResource> service) {
    super(meta, service);
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
  protected void rename(final String name) throws IOException {
    service.renameDb(meta.db, name);
  }

  @Override
  protected void copyToRoot(final String name) throws IOException {
    service.copyDb(meta.db, name);
  }

  @Override
  protected void moveToRoot(final String name) throws IOException {
    rename(name);
  }
}
