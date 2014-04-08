package org.basex.http.webdav;

import java.io.*;
import java.util.*;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.LockedException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.PreConditionFailedException;
import org.basex.util.*;

/**
 * Dummy resource to be returned when no authorization is provided.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
final class BXNotAuthorizedResource implements FolderResource, LockableResource {
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
  public boolean authorise(final Request request, final Method method, final Auth auth) {
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

  @Override
  public LockResult lock(LockTimeout lockTimeout, LockInfo lockInfo) throws NotAuthorizedException, PreConditionFailedException, LockedException {
    return null;
  }

  @Override
  public LockResult refreshLock(String s) throws NotAuthorizedException, PreConditionFailedException {
    return null;
  }

  @Override
  public void unlock(String s) throws NotAuthorizedException, PreConditionFailedException {

  }

  @Override
  public LockToken getCurrentLock() {
    return null;
  }
}
