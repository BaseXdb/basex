package org.basex.api.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.basex.core.Context;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

/**
 * WebDAV resource representing a collection database.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXCollectionDatabaseResource extends BXResource implements
    FolderResource {
  /** Database name. */
  private final String dbname;

  /**
   * Constructor.
   * @param c database context
   * @param n database name
   */
  public BXCollectionDatabaseResource(final Context c, final String n) {
    ctx = c;
    dbname = n;
  }

  @Override
  public CollectionResource createCollection(final String newName)
      throws NotAuthorizedException, ConflictException, BadRequestException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Resource child(final String childName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<? extends Resource> getChildren() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return dbname;
  }

  @Override
  public Date getModifiedDate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Resource createNew(final String newName, final InputStream inputStream,
      final Long length, final String contentType) throws IOException,
      ConflictException, NotAuthorizedException, BadRequestException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void copyTo(final CollectionResource toCollection, final String name)
      throws NotAuthorizedException, BadRequestException, ConflictException {
    // TODO Auto-generated method stub

  }

  @Override
  public void delete() throws NotAuthorizedException, ConflictException,
      BadRequestException {
    // TODO Auto-generated method stub

  }

  @Override
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType)
      throws IOException, NotAuthorizedException, BadRequestException {
    // TODO Auto-generated method stub

  }

  @Override
  public Long getMaxAgeSeconds(final Auth auth) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getContentType(final String accepts) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Long getContentLength() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void moveTo(final CollectionResource rDest, final String name)
      throws ConflictException, NotAuthorizedException, BadRequestException {
    // TODO Auto-generated method stub

  }

  @Override
  public Date getCreateDate() {
    // TODO Auto-generated method stub
    return null;
  }
}
