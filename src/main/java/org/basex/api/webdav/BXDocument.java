package org.basex.api.webdav;

import static org.basex.api.webdav.BXResourceFactory.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import org.basex.core.BaseXException;
import org.basex.core.cmd.Open;
import org.basex.server.ClientSession;

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
  /** Database containing the document. */
  private final String dbname;
  /** Path to document in database. */
  private final String docpath;

  /**
   * Constructor.
   * @param db database name
   * @param path path to document
   */
  public BXDocument(final String db, final String path) {
    dbname = db;
    docpath = path;
  }

  /**
   * Constructor.
   * @param db name of database this document belongs to.
   * @param path document path to root
   * @param u user name
   * @param p password
   */
  public BXDocument(final String db, final String path, final String u,
      final String p) {
    dbname = db;
    docpath = path;
    user = u;
    pass = p;
  }

  @Override
  public void copyTo(final CollectionResource arg0, final String arg1) {
    // TODO Auto-generated method stub

  }

  @Override
  public Date getModifiedDate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName() {
    final int idx  = docpath.lastIndexOf(DIRSEP);
    return idx < 0 ? docpath : docpath.substring(idx + 1, docpath.length());
  }

  @Override
  public void delete() {
    try {
      ClientSession cs = login(user, pass);
      try {
        cs.execute(new Open(dbname));
        cs.query("db:delete('" + docpath + "')").execute();
      } finally {
        cs.close();
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
  public String getContentType(final String arg0) {
    return MIMETYPEXML;
  }

  @Override
  public Long getMaxAgeSeconds(final Auth arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void sendContent(final OutputStream out, final Range arg1,
      final Map<String, String> arg2, final String arg3) throws IOException {
    ClientSession cs = login(user, pass);
    cs.setOutputStream(out);
    try {
      cs.query("collection('" + dbname + DIRSEP + docpath + "')").execute();
      //out.flush();
    } catch(BaseXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      cs.close();
    }
  }

  @Override
  public void moveTo(final CollectionResource arg0, final String arg1) {
    // TODO Auto-generated method stub

  }

  @Override
  public String processForm(final Map<String, String> arg0,
      final Map<String, FileItem> arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Date getCreateDate() {
    // TODO Auto-generated method stub
    return null;
  }

}
