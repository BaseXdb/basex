package org.basex.api.webdav;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import org.basex.core.Context;
import org.basex.core.Prop;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.FileResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;


/**
 * WebDAV resource representing a document in a collection database.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public final class BXDocument extends BXResource implements FileResource {

  /** Default content type. */
  private static final String MIMETYPEXML = "text/xml";
  /** Database containing the document. */
  private final String dbname;
  /** Path to document in database. */
  private final String docpath;

  /**
   * Constructor.
   * @param db database containing the document
   * @param doc path to document in databse
   * @param c context
   */
  public BXDocument(final String db, final String doc, final Context c) {
    dbname = db;
    docpath = doc;
    ctx = c;
  }
  
  @Override
  public void copyTo(CollectionResource toCollection, String name)
      throws NotAuthorizedException, BadRequestException, ConflictException {
    // TODO Auto-generated method stub

  }

  @Override
  public String getName() {
    final int idx = docpath.lastIndexOf(Prop.DIRSEP);
    return docpath.substring(idx + 1, docpath.length());
  }

  @Override
  public Date getModifiedDate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void delete() throws NotAuthorizedException, ConflictException,
      BadRequestException {
    // TODO Auto-generated method stub

  }

  @Override
  public void sendContent(OutputStream out, Range range,
      Map<String, String> params, String contentType) throws IOException,
      NotAuthorizedException, BadRequestException {
    // TODO Auto-generated method stub

  }

  @Override
  public Long getMaxAgeSeconds(Auth auth) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getContentType(String accepts) {
    return MIMETYPEXML;
  }

  @Override
  public Long getContentLength() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void moveTo(CollectionResource rDest, String name)
      throws ConflictException, NotAuthorizedException, BadRequestException {
    // TODO Auto-generated method stub

  }

  @Override
  public String processForm(Map<String, String> parameters,
      Map<String, FileItem> files) throws BadRequestException,
      NotAuthorizedException, ConflictException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Date getCreateDate() {
    // TODO Auto-generated method stub
    return null;
  }

}
