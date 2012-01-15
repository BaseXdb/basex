package org.basex.api.webdav;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;

/**
 * Dummy resource to be returned when no authorization is provided.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public final class BXNotAuthorizedResource extends BXResource implements
    FolderResource {

  /** The only instance of this class. */
  public static final Resource NOAUTH = new BXNotAuthorizedResource();

  /** Constructor. */
  private BXNotAuthorizedResource() {
    super(null, null, null);
  }

  @Override
  public Object authenticate(final String u, final String p) {
    return null;
  }

  @Override
  public boolean authorise(final Request request, final Method method,
      final Auth auth) {
    return false;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType) {
  }

  @Override
  public Long getMaxAgeSeconds(final Auth auth) {
    return null;
  }

  @Override
  public String getContentType(final String accepts) {
    return null;
  }

  @Override
  public Long getContentLength() {
    return null;
  }

  @Override
  public CollectionResource createCollection(final String newName) {
    return null;
  }

  @Override
  public Resource child(final String childName) {
    return null;
  }

  @Override
  public List<? extends Resource> getChildren() {
    return null;
  }

  @Override
  public Resource createNew(final String newName, final InputStream inputStream,
      final Long length, final String contentType) {
    return null;
  }

  @Override
  public void copyTo(final CollectionResource toCollection, final String name) {
  }

  @Override
  public void delete() {
  }

  @Override
  public void moveTo(final CollectionResource rDest, final String name) {
  }

  @Override
  public Date getCreateDate() {
    return null;
  }
}
