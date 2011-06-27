package org.basex.api.webdav;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import org.basex.core.BaseXException;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.Open;
import org.basex.server.Query;
import org.basex.server.Session;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.FileResource;
import com.bradmcevoy.http.Range;

/**
 * WebDAV resource representing an XML document.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXDocument extends BXResource implements FileResource {
  /**
   * Constructor.
   * @param dbname database name
   * @param docpath path to document
   */
  public BXDocument(final String dbname, final String docpath) {
    super(dbname, docpath);
  }

  /**
   * Constructor.
   * @param dbname name of database this document belongs to.
   * @param docpath document path to root
   * @param u user name
   * @param p password
   */
  public BXDocument(final String dbname, final String docpath, final String u,
      final String p) {
    this(dbname, docpath);
    user = u;
    pass = p;
  }

  @Override
  public void copyTo(final CollectionResource toCollection, final String name) {
    // TODO Auto-generated method stub
  }

  @Override
  public void delete() {
    try {
      final Session s = BXResourceFactory.login(user, pass);
      try {
        s.execute(new Open(db));
        s.execute(new Delete(path));
      } finally {
        s.close();
      }
    } catch(Exception e) {
      // [RS] WebDAV: error handling
      e.printStackTrace();
    }
  }

  @Override
  public Long getContentLength() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getContentType(final String accepts) {
    return MIMETYPEXML;
  }

  @Override
  public Long getMaxAgeSeconds(final Auth auth) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType)
      throws IOException {
    final Session s = BXResourceFactory.login(user, pass);
    try {
      s.setOutputStream(out);
      final Query q = s.query(
          "declare variable $path as xs:string external; " +
          "collection($path)");
      q.bind("$path", db + DIRSEP + path);
      q.execute();
    } catch(BaseXException e) {
      // [RS] WebDAV: error handling
      e.printStackTrace();
    } finally {
      s.close();
    }
  }

  @Override
  public void moveTo(final CollectionResource rDest, final String name) {
    // TODO Auto-generated method stub
  }

  @Override
  public String processForm(final Map<String, String> parameters,
      final Map<String, FileItem> files) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Date getCreateDate() {
    // TODO Auto-generated method stub
    return null;
  }
}
