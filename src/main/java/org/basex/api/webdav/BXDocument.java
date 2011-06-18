package org.basex.api.webdav;

import static org.basex.api.webdav.BXResourceFactory.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.FileResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

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
  public void copyTo(CollectionResource arg0, String arg1)
      throws NotAuthorizedException, BadRequestException, ConflictException {
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
    
    return idx < 0 ? docpath : docpath.substring(idx+1, docpath.length());
  }

  @Override
  public void delete() throws NotAuthorizedException, ConflictException,
      BadRequestException {
    // TODO Auto-generated method stub

  }

  @Override
  public Long getContentLength() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getContentType(String arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Long getMaxAgeSeconds(Auth arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void sendContent(OutputStream arg0, Range arg1,
      Map<String, String> arg2, String arg3) throws IOException,
      NotAuthorizedException, BadRequestException {
    // TODO Auto-generated method stub

  }

  @Override
  public void moveTo(CollectionResource arg0, String arg1)
      throws ConflictException, NotAuthorizedException, BadRequestException {
    // TODO Auto-generated method stub

  }

  @Override
  public String processForm(Map<String, String> arg0, Map<String, FileItem> arg1)
      throws BadRequestException, NotAuthorizedException, ConflictException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Date getCreateDate() {
    // TODO Auto-generated method stub
    return null;
  }

}
