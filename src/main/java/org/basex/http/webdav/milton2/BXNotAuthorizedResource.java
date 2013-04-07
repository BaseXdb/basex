package org.basex.http.webdav.milton2;

import io.milton.http.Auth;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.resource.CollectionResource;
import io.milton.resource.FolderResource;
import io.milton.resource.Resource;
import org.basex.core.Prop;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Dummy resource to be returned when no authorization is provided.
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 */
public final class BXNotAuthorizedResource implements FolderResource {
  /** The only instance of this class. */
  public static final Resource NOAUTH = new BXNotAuthorizedResource();

  /** Constructor. */
  private BXNotAuthorizedResource() {
  }

  @Override
  public String getRealm() {
    return Prop.NAME;
  }

  @Override
  public Date getModifiedDate() {
    return null;
  }

  @Override
  public String getUniqueId() {
    return null;
  }

  @Override
  public Object authenticate(final String u, final String p) {
    return null;
  }

  @Override
  public boolean authorise(final Request request, final Request.Method method,
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

  @Override
  public String checkRedirect(final Request request) {
    return null;
  }
}
