package org.basex.http.webdav.milton2;

import io.milton.http.Auth;
import io.milton.http.FileItem;
import io.milton.http.Range;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.resource.FileResource;
import org.basex.http.webdav.impl.ResourceMetaData;
import org.basex.http.webdav.impl.WebDAVService;
import org.basex.util.Util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import static org.basex.http.webdav.impl.Utils.SEP;
import static org.basex.http.webdav.impl.Utils.dbname;

/**
 * WebDAV resource representing a file.
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 */
public class BXFile extends BXAbstractResource implements FileResource {
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
    final Map<String, FileItem> files) throws BadRequestException {
    return null;
  }

  @Override
  public String getContentType(final String accepts) {
    return meta.ctype;
  }

  @Override
  public void sendContent(final OutputStream out, final Range range, final Map<String,
    String> params, final String contentType) throws IOException,
    NotAuthorizedException, BadRequestException, NotFoundException {
    try {
      service.retrieve(meta.db, meta.path, meta.raw, out);
    } catch(IOException e) {
      Util.errln(e);
      throw new BadRequestException(this, e.getMessage());
    }
  }

  @Override
  protected void copyToRoot(final String n) throws IOException {
    // document is copied to the root: create new database with it
    final String nm = dbname(n);
    service.createDb(nm);
    service.copyDoc(meta.db, meta.path, nm, n);
  }

  @Override
  protected void copyTo(final BXFolder f, final String n) throws IOException {
    // folder is copied to a folder in a database
    service.copyDoc(meta.db, meta.path, f.meta.db, f.meta.path + SEP + n);
    service.deleteDummy(f.meta.db, f.meta.path);
  }
}
