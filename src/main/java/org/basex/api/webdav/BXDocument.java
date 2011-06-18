package org.basex.api.webdav;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.FileResource;
import com.bradmcevoy.http.Range;

/**
 * WebDAV resource representing a document in a collection database.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public final class BXDocument extends BXResource implements FileResource {
  /** Database containing the document. */
  private final String dbname;
  /** Path to document in database. */
  private final String docpath;
  /** PRE value of the document. */
  private final int preval;

  /**
   * Constructor.
   * @param db database containing the document
   * @param doc path to document in database
   * @param pre PRE value of the document
   */
  public BXDocument(final String db, final String doc, final int pre) {
    dbname = db;
    docpath = doc;
    preval = pre;
  }

  @Override
  public void copyTo(final CollectionResource toCollection, final String name) {
    // TODO Auto-generated method stub
  }

  @Override
  public String getName() {
    final int idx = docpath.lastIndexOf('/');
    return docpath.substring(idx + 1, docpath.length());
  }

  @Override
  public Date getModifiedDate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void delete() {
    // TODO Auto-generated method stub
  }

  @Override
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType)
      throws IOException {
    // try {
    // new Open(dbname).execute(ctx);
    // final XMLSerializer ser = new XMLSerializer(out);
    // new Nodes(preval, ctx.data).serialize(ser);
    // ser.close();
    // new Close().execute(ctx);
    // } catch(BaseXException e) {
    // // [DP] WebDAV: error handling
    // e.printStackTrace();
    // }
  }

  @Override
  public Long getMaxAgeSeconds(final Auth auth) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getContentType(final String accepts) {
    return MIMETYPEXML;
  }

  @Override
  public Long getContentLength() {
    // TODO Auto-generated method stub
    return null;
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
