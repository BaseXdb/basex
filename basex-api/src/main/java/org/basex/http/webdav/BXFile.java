package org.basex.http.webdav;

import static org.basex.http.webdav.impl.Utils.*;

import java.io.*;
import java.util.*;

import org.basex.http.webdav.impl.ResourceMetaData;
import org.basex.http.webdav.impl.WebDAVService;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.*;

/**
 * WebDAV resource representing a file.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public final class BXFile extends BXAbstractResource implements FileResource {
  /**
   * Constructor.
   * @param d resource meta data
   * @param s service implementation
   */
  public  BXFile(final ResourceMetaData d, final WebDAVService<BXAbstractResource> s) {
    super(d, s);
  }

  @Override
  public Long getContentLength() {
    return meta.size;
  }

  @Override
  public Date getCreateDate() {
    return null;
  }

  @Override
  public Long getMaxAgeSeconds(final Auth auth) {
    return null;
  }

  @Override
  public String processForm(final Map<String, String> parameters,
      final Map<String, FileItem> files) {
    return null;
  }

  @Override
  public String getContentType(final String accepts) {
    return meta.ctype;
  }

  @Override
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType) throws BadRequestException {
    new BXCode<Object>(this) {
      @Override
      public void run() throws IOException {
        service.retrieve(meta.db, meta.path, meta.raw, out);
      }
    }.eval();
  }

  @Override
  protected void copyToRoot(final String name) throws IOException {
    // document is copied to the root: create new database with it
    final String nm = dbname(name);
    service.createDb(nm);
    service.copyDoc(meta.db, meta.path, nm, name);
  }

  @Override
  protected void copyTo(final BXFolder folder, final String name) throws IOException {
    // folder is copied to a folder in a database
    service.copyDoc(meta.db, meta.path, folder.meta.db, folder.meta.path + SEP + name);
    service.deleteDummy(folder.meta.db, folder.meta.path);
  }
}
